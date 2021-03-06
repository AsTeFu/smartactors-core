package info.smart_tools.smartactors.ioc.istrategy_container;

import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.ioc.istrategy_container.exception.StrategyContainerException;

/**
 * StrategyContainer interface
 * Provides methods for resolve and register dependency
 * {@link IResolveDependencyStrategy}
 * by object unique identifier
 */
public interface IStrategyContainer {

    /**
     * Resolve dependency of {@link IResolveDependencyStrategy} by unique object identifier
     * @param key unique object identifier
     * @return instance of {@link IResolveDependencyStrategy}
     * @throws StrategyContainerException if any error occurred
     */
    IResolveDependencyStrategy resolve(final Object key)
            throws StrategyContainerException;

    /**
     * Register new dependency of {@link IResolveDependencyStrategy} by unique object identifier
     * @param key unique object identifier
     * @param strategy instance of {@link IResolveDependencyStrategy}
     * @throws StrategyContainerException  if any error occurred
     */
    void register(final Object key, final IResolveDependencyStrategy strategy)
            throws StrategyContainerException;

    /**
     * Remove existing dependency of {@link IResolveDependencyStrategy} by unique object identifier
     * @param key unique object identifier
     * @throws StrategyContainerException  if any error occurred
     */
    void remove(final Object key)
            throws StrategyContainerException;
}
