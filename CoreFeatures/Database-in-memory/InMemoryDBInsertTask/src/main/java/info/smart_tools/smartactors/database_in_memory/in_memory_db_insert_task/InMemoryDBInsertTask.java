package info.smart_tools.smartactors.database_in_memory.in_memory_db_insert_task;

import info.smart_tools.smartactors.database.interfaces.idatabase.IDatabase;
import info.smart_tools.smartactors.database.interfaces.idatabase.exception.IDatabaseException;
import info.smart_tools.smartactors.database.interfaces.idatabase_task.IDatabaseTask;
import info.smart_tools.smartactors.database.interfaces.idatabase_task.exception.TaskPrepareException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.database_in_memory.in_memory_database.InMemoryDatabase;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ReadValueException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.task.interfaces.itask.exception.TaskExecutionException;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;

/**
 * The task to insert the document in the in-memory database.
 */
public class InMemoryDBInsertTask implements IDatabaseTask {
    private IFieldName collectionNameFieldName;
    private IFieldName documentFieldName;

    private String collectionName;
    private IObject document;

    /**
     * Creates the task.
     * @throws TaskPrepareException if not possible to resolve IFieldName
     */
    public InMemoryDBInsertTask() throws TaskPrepareException {
        try {
            collectionNameFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "collectionName");
            documentFieldName = IOC.resolve(Keys.getOrAdd(IFieldName.class.getCanonicalName()), "document");
        } catch (ResolutionException e) {
            throw new TaskPrepareException("Failed to resolve \"IFieldName\"", e);
        }
    }

    @Override
    public void prepare(final IObject query) throws TaskPrepareException {
        try {
            collectionName = (String) query.getValue(collectionNameFieldName);
            document = (IObject) query.getValue(documentFieldName);
        } catch (ReadValueException | InvalidArgumentException e) {
            throw new TaskPrepareException("Failed to getting values from query", e);
        }
    }

    @Override
    public void execute() throws TaskExecutionException {
        try {
            IDatabase dataBase = IOC.resolve(Keys.getOrAdd(InMemoryDatabase.class.getCanonicalName()));
            dataBase.insert(document, collectionName);
        } catch (ResolutionException e) {
            throw new TaskExecutionException("Failed to resolve InMemoryDatabase", e);
        } catch (IDatabaseException e) {
            throw new TaskExecutionException("Failed to upsert document into " + collectionName, e);
        }
    }
}