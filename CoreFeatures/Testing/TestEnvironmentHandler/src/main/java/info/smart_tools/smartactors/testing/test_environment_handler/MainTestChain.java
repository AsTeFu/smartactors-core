package info.smart_tools.smartactors.testing.test_environment_handler;

import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.base.exception.initialization_exception.InitializationException;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.iobject.iobject.exception.ChangeValueException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageReceiver;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.MessageReceiveException;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.exceptions.NestedChainStackOverflowException;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation of {@link IReceiverChain} used as a root chain for test messages. Calls a callback when chain execution completed.
 */
public class MainTestChain implements IReceiverChain {
    private IAction<Throwable> completionCallback;
    private IObject successReceiverArgs;
    private IObject testChainReceiverArgs;
    private AtomicBoolean isCompleted;
    private IReceiverChain testChain;

    private IFieldName chainFieldName;

    private IMessageReceiver testChainRunnerReceiver = mp -> {
        try {
            mp.getSequence().callChain(this.testChain);
        } catch (NestedChainStackOverflowException e) {
            throw new MessageReceiveException(e);
        }
    };

    private IMessageReceiver successfulReceiver = mp -> {
        try {
            if (isCompleted.compareAndSet(false, true)) {
                completionCallback.execute(null);
            }
        } catch (ActionExecuteException | InvalidArgumentException e) {
            throw new MessageReceiveException(e);
        }
    };

    /**
     * The constructor.
     *
     * @param chain the testing chain
     * @param completionCallback    the callback that should be called when chain completes successful (with {@code null} as the only
     *                              argument) or with exception (with that exception as first argument)
     * @param successReceiverArgs   object that will e returned by {@link #getArguments(int)} for a receiver reached in case of successful
     *                              completion of a chain
     * @throws InvalidArgumentException if {@code completionCallback} is {@code null}
     * @throws InitializationException if resolution dependency for {@link IObject} was failed
     */
    public MainTestChain(final IReceiverChain chain, final IAction<Throwable> completionCallback, final IObject successReceiverArgs)
            throws InvalidArgumentException, InitializationException {
        if (null == completionCallback) {
            throw new InvalidArgumentException("Callback should not be null.");
        }
        if (null == chain) {
            throw new InvalidArgumentException("Test chain should not be null.");
        }
        this.testChain = chain;
        this.completionCallback = completionCallback;
        this.successReceiverArgs = successReceiverArgs;
        this.isCompleted = new AtomicBoolean(false);
        try {
            this.chainFieldName = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), "info.smart_tools.smartactors.iobject.ifield_name.IFieldName"), "chain");
            this.testChainReceiverArgs = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), "info.smart_tools.smartactors.iobject.iobject.IObject"));
            if (null == this.successReceiverArgs) {
                this.successReceiverArgs = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), "info.smart_tools.smartactors.iobject.iobject.IObject"));
            }
        } catch (ResolutionException e) {
            throw new InitializationException("Could not resolve dependency for IObject.", e);
        }
    }

    @Override
    public IMessageReceiver get(final int index) {
        if (index == 0) {
            return this.testChainRunnerReceiver;
        }
        if (index == 1) {
            return this.successfulReceiver;
        }
        return null;
    }

    @Override
    public IObject getArguments(final int index) {
            return (index == 0) ?
                    this.testChainReceiverArgs :
                    this.successReceiverArgs;
    }

    @Override
    public String getName() {
        return "root test chain";
    }

    @Override
    public IObject getExceptionalChainAndEnvironments(final Throwable exception) {
        IObject exceptionalChainAndEnv = null;
        try {
            exceptionalChainAndEnv = IOC.resolve(IOC.resolve(IOC.getKeyForKeyStorage(), "info.smart_tools.smartactors.iobject.iobject.IObject"));
            exceptionalChainAndEnv.setValue(this.chainFieldName, new ExceptionalTestChain());
            if (isCompleted.compareAndSet(false, true)) {
                completionCallback.execute(exception);
            }
        } catch (ActionExecuteException | InvalidArgumentException | ResolutionException | ChangeValueException e) {
            e.printStackTrace();
        }



        return exceptionalChainAndEnv;
    }

    @Override
    public IObject getChainDescription() {
        return null;
    }

    @Override
    public Collection<IReceiverChain> getExceptionalChains() {
        return Collections.emptyList();
    }
}
