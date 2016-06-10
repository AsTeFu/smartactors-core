package info.smart_tools.smartactors.core.db_task.upsert.psql;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.exceptions.StorageException;
import info.smart_tools.smartactors.core.db_storage.interfaces.CompiledQuery;
import info.smart_tools.smartactors.core.db_storage.interfaces.StorageConnection;
import info.smart_tools.smartactors.core.db_storage.utils.CollectionName;
import info.smart_tools.smartactors.core.db_task.upsert.psql.exception.DBUpsertTaskException;
import info.smart_tools.smartactors.core.db_task.upsert.psql.wrapper.UpsertMessage;
import info.smart_tools.smartactors.core.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.core.idatabase_task.exception.TaskSetConnectionException;
import info.smart_tools.smartactors.core.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.core.iobject.IFieldName;
import info.smart_tools.smartactors.core.iobject.IObject;
import info.smart_tools.smartactors.core.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.core.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.core.ioc.IOC;
import info.smart_tools.smartactors.core.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.core.sql_commons.JDBCCompiledQuery;
import info.smart_tools.smartactors.core.sql_commons.QueryStatement;

import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Task for upsert row to collection:
 * Executes update operation if incoming query contains id
 * Executes insert operation otherwise
 */
public class DBUpsertTask implements IDatabaseTask {

    private static final String INSERT_MODE = "insert";
    private static final String UPDATE_MODE = "update";

    private String collectionName;
    private DBInsertTask dbInsertTask;
    private IObject rawUpsertQuery;
    private CompiledQuery compiledQuery;
    private QueryStatement updateQueryStatement;
    private QueryStatement insertQueryStatement;
    private StorageConnection connection;
    private Map<String, UpsertExecution> executionMap;
    private String mode;
    private IFieldName idFieldName;

    private interface UpsertExecution {
        void upsert() throws TaskExecutionException;

    }

    public DBUpsertTask() throws DBUpsertTaskException {

        executionMap = new HashMap<>();
        executionMap.put(UPDATE_MODE, () -> {
            try {
                int nUpdated = ((JDBCCompiledQuery)compiledQuery).getPreparedStatement().executeUpdate();
                if (nUpdated == 0) {
                    throw new TaskExecutionException("Update query failed: wrong count of documents is updated.");
                }
            } catch (SQLException e) {
                throw new TaskExecutionException("Transaction execution has been failed.", e);
            }
        });
        executionMap.put(INSERT_MODE, () -> {
            try {
                ResultSet resultSet = ((JDBCCompiledQuery)compiledQuery).getPreparedStatement().executeQuery();
                if (resultSet.first()) {
                    try {
                        //TODO:: replace by field.inject()
                        rawUpsertQuery.setValue(idFieldName, resultSet.getLong("id"));
                    } catch (ChangeValueException e) {
                        throw new TaskExecutionException("Could not set new id on inserted document.");
                    }
                } else {
                    throw new TaskExecutionException("Database returned not enough of generated ids.");
                }
            } catch (SQLException e) {
                throw new TaskExecutionException("Insertion query execution failed because of SQL exception.",e);
            }
        });
    }

    @Override
    public void prepare(final IObject upsertObject) throws TaskPrepareException {

        try {
            UpsertMessage upsertMessage = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), UpsertMessage.class.toString()), upsertObject);
            this.collectionName = upsertMessage.getCollectionName();
            this.dbInsertTask = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), DBInsertTask.class.toString()));
            this.updateQueryStatement = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), QueryStatement.class.toString()));
            this.insertQueryStatement = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), QueryStatement.class.toString()));
        } catch (ResolutionException e) {
            throw new TaskPrepareException("Error while resolving query statement.", e);
        } catch (ReadValueException | ChangeValueException e) {
            throw new TaskPrepareException("Error while get collection name.", e);
        }
        initUpdateQuery();
        //TODO:: move to DBInsertTask constructor or to the separate class
        initInsertQuery();
        try {
            this.idFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), IFieldName.class.toString()), collectionName + "Id");
        } catch (ResolutionException e) {
            throw new TaskPrepareException("Can't create idFieldName.", e);
        }


        this.rawUpsertQuery = upsertObject;
        try {
            String id = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), String.class.toString()), upsertObject.getValue(idFieldName));
            if (id != null) {
                this.mode = UPDATE_MODE;
                updateQueryStatement.pushParameterSetter((statement, index) -> {
                    try {
                        statement.setLong(index++, Long.parseLong(id));
                        statement.setString(index++, upsertObject.toString());
                    } catch (NullPointerException | NumberFormatException e) {
                        throw new QueryBuildException("Error while writing update query statement: ", e);
                    }
                    return index;
                });
                this.compiledQuery = connection.compileQuery(updateQueryStatement);
            } else {
                this.mode = INSERT_MODE;
                dbInsertTask.setConnection(connection);
                dbInsertTask.prepare(upsertObject);

                //TODO:: move to DBInsertTask prepare() or to the separate class
//                insertQueryStatement.pushParameterSetter((statement, index) -> {
//
//                    statement.setString(index++, message.toString());
//                    return index;
//                });


                this.compiledQuery = dbInsertTask.getCompiledQuery();
            }
        } catch (ReadValueException | StorageException | ResolutionException | TaskSetConnectionException e) {
            throw new TaskPrepareException("Error while writing update query statement.",e);
        }
    }

    @Override
    public void setConnection(final StorageConnection connection) throws TaskSetConnectionException {
        this.connection = connection;
    }

    @Override
    public void execute() throws TaskExecutionException {

        executionMap.get(mode).upsert();
    }

    private void initUpdateQuery() throws TaskPrepareException {

        Writer writer = updateQueryStatement.getBodyWriter();
        try {
            writer.write(String.format(
                "UPDATE %s AS tab SET %s = docs.document FROM (VALUES", CollectionName.fromString(collectionName).toString(), "document"
            ));
            writer.write("(?,?::jsonb)");
            writer.write(String.format(") AS docs (id, document) WHERE tab.%s = docs.id;", "id"));
        } catch (IOException | QueryBuildException e) {
            throw new TaskPrepareException("Error while initialize update query.", e);
        }
    }

    private void initInsertQuery() throws TaskPrepareException {

        Writer writer = insertQueryStatement.getBodyWriter();
        try {
            writer.write(String.format(
                "INSERT INTO %s (%s) VALUES", CollectionName.fromString(collectionName).toString(), "document"
            ));
            writer.write("(?::jsonb)");
            writer.write(String.format(" RETURNING %s AS id;", "id"));
        } catch (IOException | QueryBuildException e) {
            throw new TaskPrepareException("Error while initialize insert query.", e);
        }
    }
}
