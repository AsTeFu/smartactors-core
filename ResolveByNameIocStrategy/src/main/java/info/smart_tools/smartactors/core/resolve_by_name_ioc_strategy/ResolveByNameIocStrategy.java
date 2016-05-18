package info.smart_tools.smartactors.core.resolve_by_name_ioc_strategy;

import info.smart_tools.smartactors.core.ikey.IKey;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.core.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import info.smart_tools.smartactors.core.string_ioc_key.Key;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link IResolveDependencyStrategy}
 * <pre>
 * Strategy allows to storage instances of {@link info.smart_tools.smartactors.core.ikey.IKey}
 * </pre>
 */
public class ResolveByNameIocStrategy implements IResolveDependencyStrategy {

    /**
     * Local {@link info.smart_tools.smartactors.core.ikey.IKey} instance storage
     */
    private Map<String, IKey> storage = new ConcurrentHashMap<>();

    /**
     * Default constructor
     */
    public ResolveByNameIocStrategy() {
    }

    /**
     * Return stored instance of {@link info.smart_tools.smartactors.core.ikey.IKey} if exists
     * otherwise create new instance of {@link IKey}, store to the local storage and return
     * @param <T> type of object
     * @param args needed parameters for resolve dependency
     * @return instance of object
     * @throws ResolveDependencyStrategyException if any errors occurred
     */
    @Override
    public <T> T resolve(final Object... args)
            throws ResolveDependencyStrategyException {
        try {
            IKey result = storage.get((String) args[0]);
            if (result == null) {
                result = new Key(String.class, (String) args[0]);
                storage.put((String) args[0], result);
            }
            return (T) result;
        } catch (Exception e) {
            throw new ResolveDependencyStrategyException("Object resolution failed.", e);
        }
    }
}
