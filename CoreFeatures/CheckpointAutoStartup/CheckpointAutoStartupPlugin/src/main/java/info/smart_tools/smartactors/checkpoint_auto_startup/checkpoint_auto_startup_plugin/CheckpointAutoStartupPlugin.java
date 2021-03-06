package info.smart_tools.smartactors.checkpoint_auto_startup.checkpoint_auto_startup_plugin;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.base.isynchronous_service.exceptions.IllegalServiceStateException;
import info.smart_tools.smartactors.base.isynchronous_service.exceptions.ServiceStartupException;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.feature_loading_system.bootstrap_plugin.BootstrapPlugin;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.scheduler.interfaces.ISchedulerService;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.task.interfaces.itask.exception.TaskExecutionException;

public class CheckpointAutoStartupPlugin extends BootstrapPlugin {

    /**
     * The constructor.
     *
     * @param bootstrap    the bootstrap
     */
    public CheckpointAutoStartupPlugin(final IBootstrap bootstrap) {
            super(bootstrap);
    }

    /**
     * Register a action that will start scheduler service of checkpoint actor after feature group load completion.
     *
     * @throws ResolutionException if error occurs resolving the key
     * @throws RegistrationException if error occurs registering the strategy
     * @throws InvalidArgumentException if some unexpected error suddenly occurs
     */
    @Item("checkpoint_actor_delayed_startup_action")
    public void doSomeThing()
            throws ResolutionException, RegistrationException, InvalidArgumentException {
        IOC.register(Keys.getOrAdd("scheduler service activation action for checkpoint actor"),
                new SingletonStrategy((IAction<ISchedulerService>) service -> {
                    try {
                        IQueue<ITask> featureLoadCompletionQueue = IOC.resolve(Keys.getOrAdd("feature group load completion task queue"));
                        featureLoadCompletionQueue.put(() -> {
                            try {
                                service.start();
                            } catch (ServiceStartupException | IllegalServiceStateException e) {
                                throw new TaskExecutionException(e);
                            }
                        });
                    } catch (ResolutionException e) {
                        throw new ActionExecuteException(e);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }));
    }
}
