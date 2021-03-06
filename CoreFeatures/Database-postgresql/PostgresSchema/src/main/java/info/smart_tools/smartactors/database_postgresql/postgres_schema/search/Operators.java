package info.smart_tools.smartactors.database_postgresql.postgres_schema.search;

import info.smart_tools.smartactors.database.database_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.database_postgresql.postgres_connection.QueryStatement;
import info.smart_tools.smartactors.database_postgresql.postgres_schema.PostgresSchema;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * A set of methods which writes query parts for comparison operators and their parameters.
 */
final class Operators {

    /**
     * Private constructor to avoid instantiation.
     */
    private Operators() {
    }

    /**
     * Registers all operators to the resolver.
     * @param resolver resolver to write queries with all these operators
     */
    public static void addAll(final PostgresQueryWriterResolver resolver) {

        // Basic field comparison operators
        resolver.addQueryWriter("$eq", formattedCheckWriter("((%s)=to_json(?)::jsonb)"));
        resolver.addQueryWriter("$ne", formattedCheckWriter("((%s)!=to_json(?)::jsonb)"));
        resolver.addQueryWriter("$lt", formattedCheckWriter("((%s)<to_json(?)::jsonb)"));
        resolver.addQueryWriter("$gt", formattedCheckWriter("((%s)>to_json(?)::jsonb)"));
        resolver.addQueryWriter("$lte", formattedCheckWriter("((%s)<=to_json(?)::jsonb)"));
        resolver.addQueryWriter("$gte", formattedCheckWriter("((%s)>=to_json(?)::jsonb)"));

        //Check on present
        resolver.addQueryWriter("$isNull", Operators::writeFieldExistsCheckCondition);

        // ISO 8601 date/time operators
        /*TODO: Find a way to build an index on date/time field.*/
        resolver.addQueryWriter("$date-from", formattedCheckWriter("(parse_timestamp_immutable(%s)>=(?)::timestamp)"));
        resolver.addQueryWriter("$date-to", formattedCheckWriter("(parse_timestamp_immutable(%s)<=(?)::timestamp)"));

        // Value in list check
        resolver.addQueryWriter("$in", Operators::writeFieldInArrayCheckCondition);

        // Tags operators
        resolver.addQueryWriter("$hasTag", formattedCheckWriter("((%s)??(?))"));

        // Fulltext search
        resolver.addQueryWriter("$fulltext", formattedCheckWriter(
                String.format("%s@@(to_tsquery('%s',?))", PostgresSchema.FULLTEXT_COLUMN, PostgresSchema.FTS_DICTIONARY)));
    }

    /**
     * Creates the condition writer based on the format string.
     * @param format format string, contains '%s' for field path and '?' for parameters
     * @return the condition writer ready to be added to basic resolver
     */
    private static QueryWriter formattedCheckWriter(final String format) {
        return (query, resolver, contextFieldPath, queryParameter) ->
                writeFieldCheckCondition(format, query, contextFieldPath, queryParameter);
    }

    /**
     * Writes part of sql query with basic field comparison operators
     * @param format sql string for condition. Contains '%s' for field path and '?' for parameters
     * @param query query statement object where to write the body and add parameter setters
     * @param contextFieldPath current field path, for example document#>'{field}'
     * @param queryParameter current value of the query parameter
     * @throws QueryBuildException if something goes wrong
     */
    private static void writeFieldCheckCondition(
            final String format,
            final QueryStatement query,
            final FieldPath contextFieldPath,
            final Object queryParameter
    ) throws QueryBuildException {

        if (contextFieldPath == null) {
            throw new QueryBuildException("Field check conditions not allowed outside of field context");
        }

        try {
            query.getBodyWriter().write(String.format(format, contextFieldPath.toSQL()));

            query.pushParameterSetter((statement, index) -> {
                statement.setObject(index++, queryParameter);
                return index;
            });
        } catch (IOException e) {
            throw new QueryBuildException("Query search conditions write failed because of exception", e);
        }
    }

    /**
     * Writes part of sql query which checks existence of field in the document
     * @param query query statement object where to write body and add parameter setters
     * @param resolver resolver for nested operators, is ignored here
     * @param contextFieldPath current field path, for example document#>'{field}'
     * @param queryParameter current parameter value. Must be boolean: if 'true' check field is null, if 'false' check the field is not null
     * @throws QueryBuildException if something goes wrong
     */
    private static void writeFieldExistsCheckCondition(
            final QueryStatement query,
            final QueryWriterResolver resolver,
            final FieldPath contextFieldPath,
            final Object queryParameter
    ) throws QueryBuildException {

        if (contextFieldPath == null) {
            throw new QueryBuildException("Field check conditions not allowed outside of field context.");
        }

        try {
            String isNullStr = String.valueOf(queryParameter);
            if (!(isNullStr.equalsIgnoreCase("true") || isNullStr.equalsIgnoreCase("false"))) {
                throw new QueryBuildException("Parameter for existence checking should represent boolean value.");
            }
            Boolean isNull = Boolean.parseBoolean(isNullStr);
            String condition = isNull ? "(%s) is null" : "(%s) is not null";
            query.getBodyWriter().write(String.format(condition, contextFieldPath.toSQL()));
        } catch (IOException e) {
            throw new QueryBuildException("Query search conditions write failed because of exception.", e);
        }
    }

    /**
     * Writes part of sql query which checks the value is presented in the array.
     * @param query query statement object where to write body and add parameter setters
     * @param resolver resolver for nested operators, is ignored here
     * @param contextFieldPath current field path, for example document#>'{field}'
     * @param queryParameter current parameter value
     * @throws QueryBuildException if something goes wrong
     */
    private static void writeFieldInArrayCheckCondition(
            final QueryStatement query,
            final QueryWriterResolver resolver,
            final FieldPath contextFieldPath,
            final Object queryParameter
    ) throws QueryBuildException {

        if (contextFieldPath == null) {
            throw new QueryBuildException("Operator \"$in\" not allowed outside of field context");
        }

        if (!(queryParameter instanceof List)) {
            throw new QueryBuildException("\"$in\" operator must be applied only to JSON array");
        }

        List paramAsList = (List) queryParameter;
        Writer writer = query.getBodyWriter();

        try {
            if (paramAsList.size() == 0) {
                writer.write("(FALSE)");
                return;
            }

            writer.write(String.format("((%s)in(", contextFieldPath.toSQL()));

            for (int i = paramAsList.size(); i > 0; --i) {
                writer.write(String.format("to_json(?)::jsonb%s", (i == 1) ? "" : ","));
            }

            writer.write("))");

            query.pushParameterSetter((statement, index) -> {
                for (Object obj : paramAsList) {
                    statement.setObject(index++, obj);
                }
                return index;
            });

        } catch (IOException e) {
            throw new QueryBuildException("Query search conditions write failed because of exception", e);
        }
    }
}
