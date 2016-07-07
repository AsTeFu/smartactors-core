package info.smart_tools.smartactors.core.db_tasks.psql.update;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.named_keys_storage.Keys;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 *
 */
class QueryStatementBuilder {
    private String collection;

    private static final String[] TEMPLATE_PARTS = {"UPDATE ",
            " AS tab SET document = docs.document FROM VALUES(?,?::jsonb)) " +
                    "AS docs (id, document) WHERE tab.id = docs.id;" };

    private static final int TEMPLATE_SIZE = TEMPLATE_PARTS[0].length() + TEMPLATE_PARTS[1].length();

    /**
     *
     */
    protected QueryStatementBuilder() {}

    public static QueryStatementBuilder create() {
        return new QueryStatementBuilder();
    }

    /**
     *
     * @param collectionName
     * @return
     */
    QueryStatementBuilder withCollection(@Nonnull final String collectionName) {
        collection = collectionName;
        return this;
    }

    /**
     *
     * @return
     * @throws QueryBuildException
     */
    QueryStatement build() throws QueryBuildException {
        try {
            requiresNonnull(collection, "The collection should not be a null or empty, should try invoke 'withCollection'.");

            QueryStatement preparedQuery = IOC.resolve(Keys.getOrAdd(QueryStatement.class.toString()));
            StringBuilder queryBuilder = new StringBuilder(TEMPLATE_SIZE + collection.length());

            queryBuilder
                    .append(TEMPLATE_PARTS[0])
                    .append(collection)
                    .append(TEMPLATE_PARTS[1]);
            preparedQuery.getBodyWriter().write(queryBuilder.toString());

            return preparedQuery;
        } catch (IOException | ResolutionException | IllegalArgumentException e) {
            throw new QueryBuildException("A query statement building error: " + e.getMessage(), e);
        }
    }

    private void requiresNonnull(final String str, final String message) throws QueryBuildException {
        if (str == null || str.isEmpty()) {
            throw new QueryBuildException(message);
        }
    }
}
