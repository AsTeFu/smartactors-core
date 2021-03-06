package info.smart_tools.smartactors.message_bus_service_starter.message_bus_starter;

import info.smart_tools.smartactors.base.strategy.create_new_instance_strategy.CreateNewInstanceStrategy;
import info.smart_tools.smartactors.iobject.ds_object.DSObject;
import info.smart_tools.smartactors.iobject.field_name.FieldName;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.message_processing_interfaces.ichain_storage.IChainStorage;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.RegistrationException;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.message_bus.interfaces.imessage_bus_handler.IMessageBusHandler;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.iobject.iobject.IObject;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.message_processing_interfaces.iresponse_strategy.IResponseStrategy;
import info.smart_tools.smartactors.task.interfaces.iqueue.IQueue;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.scope.iscope.IScope;
import info.smart_tools.smartactors.scope.iscope_provider_container.exception.ScopeProviderException;
import info.smart_tools.smartactors.task.interfaces.itask.ITask;
import info.smart_tools.smartactors.message_bus.message_bus.MessageBus;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor;
import info.smart_tools.smartactors.message_processing_interfaces.message_processing.IReceiverChain;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_strategy.ResolveByNameIocStrategy;
import info.smart_tools.smartactors.scope.scope_provider.ScopeProvider;
import info.smart_tools.smartactors.base.strategy.singleton_strategy.SingletonStrategy;
import info.smart_tools.smartactors.ioc.strategy_container.StrategyContainer;
import info.smart_tools.smartactors.base.strategy.apply_function_to_arguments.ApplyFunctionToArgumentsStrategy;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MessageBusSectionProcessingStrategyTest {

    private IQueue<ITask> taskQueue;
    private Object mapId;
    private IChainStorage chainStorage;
    private IReceiverChain receiverChain;
    private IAction responseAction;

    private IResponseStrategy nullResponseStrategy;
    private IResponseStrategy mbResponseStrategy;

    @Before
    public void setUp()
            throws ScopeProviderException, RegistrationException, ResolutionException, InvalidArgumentException {
        taskQueue = mock(IQueue.class);
        mapId = mock(Object.class);
        receiverChain = mock(IReceiverChain.class);
        chainStorage = mock(IChainStorage.class);
        ScopeProvider.subscribeOnCreationNewScope(
                scope -> {
                    try {
                        scope.setValue(IOC.getIocKey(), new StrategyContainer());
                    } catch (Exception e) {
                        throw new Error(e);
                    }
                }
        );

        Object keyOfMainScope = ScopeProvider.createScope(null);
        IScope mainScope = ScopeProvider.getScope(keyOfMainScope);
        ScopeProvider.setCurrentScope(mainScope);
        IOC.register(
                IOC.getKeyForKeyStorage(),
                new ResolveByNameIocStrategy()
        );

        IKey iFieldNameKey = Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName");

        IOC.register(iFieldNameKey,
                new CreateNewInstanceStrategy(
                        (args) -> {
                            try {
                                return new FieldName((String) args[0]);
                            } catch (InvalidArgumentException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                )
        );

        IKey chainIdFromMapNameKey = Keys.getOrAdd("chain_id_from_map_name");
        IOC.register(chainIdFromMapNameKey,
                new SingletonStrategy(mapId));

        IKey taskQueueKey = Keys.getOrAdd("task_queue");
        IOC.register(taskQueueKey,
                new SingletonStrategy(taskQueue));

        IKey chainStorageKey = Keys.getOrAdd(IChainStorage.class.getCanonicalName());
        IOC.register(chainStorageKey,
                new SingletonStrategy(chainStorage));

        responseAction = mock(IAction.class);
        IOC.register(Keys.getOrAdd("send response action"), new SingletonStrategy(responseAction));

        nullResponseStrategy = mock(IResponseStrategy.class);
        mbResponseStrategy = mock(IResponseStrategy.class);

        IOC.register(Keys.getOrAdd("null response strategy"), new SingletonStrategy(nullResponseStrategy));
        IOC.register(Keys.getOrAdd("message bus response strategy"), new SingletonStrategy(mbResponseStrategy));
    }

    @Test
    public void testLoadingConfig()
            throws Exception {
        IResolveDependencyStrategy sequenceStrategy = mock(IResolveDependencyStrategy.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), "info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessingSequence"),
                sequenceStrategy
        );
        IResolveDependencyStrategy messageProcessorStrategy = mock(IResolveDependencyStrategy.class);
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), "info.smart_tools.smartactors.message_processing_interfaces.message_processing.IMessageProcessor"),
                messageProcessorStrategy
        );
        IOC.register(
                IOC.resolve(IOC.getKeyForKeyStorage(), "info.smart_tools.smartactors.iobject.iobject.IObject"),
                new ApplyFunctionToArgumentsStrategy(
                        (args) -> {
                            return new DSObject();
                        }
                )
        );

        when(chainStorage.resolve(mapId)).thenReturn(receiverChain);
        DSObject config = new DSObject("\n" +
                "     {\n" +
                "         \"messageBus\": \n" +
                "             {\n" +
                "                 \"routingChain\": \"mainChain\",\n" +
                "                 \"stackDepth\": 5\n" +
                "             }\n" +
                "         \n" +
                "     }");
        MessageBusSectionProcessingStrategy strategy = new MessageBusSectionProcessingStrategy();
        strategy.onLoadConfig(config);

        IMessageProcessingSequence sequence = mock(IMessageProcessingSequence.class);
        when(sequenceStrategy.resolve(5, this.receiverChain)).thenReturn(sequence);
        IMessageProcessor processor = mock(IMessageProcessor.class);
        when(messageProcessorStrategy.resolve(this.taskQueue, sequence)).thenReturn(processor);
        IObject result = new DSObject();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                result.setValue(new FieldName("message"), invocationOnMock.getArguments()[0]);
                result.setValue(new FieldName("context"), invocationOnMock.getArguments()[1]);
                return null;
            }
        }).when(processor).process(any(IObject.class), any(IObject.class));

        IMessageBusHandler handler = (IMessageBusHandler) ScopeProvider.getCurrentScope().getValue(MessageBus.getMessageBusKey());
        assertNotNull(handler);
        Object replyToChainName = mock(Object.class);
        IObject message = mock(IObject.class);
        handler.handleForReply(message, replyToChainName);
        assertSame(result.getValue(new FieldName("message")), message);
        IObject resultContext = (IObject) result.getValue(new FieldName("context"));
        List<IAction> actions = (List<IAction>) resultContext.getValue(new FieldName("finalActions"));
        assertEquals(actions.size(), 1);
        IAction<IObject> action = actions.get(0);
        IObject env = new DSObject();
        IObject messageForReply = new DSObject();
        env.setValue(new FieldName("message"), messageForReply);
        env.setValue(new FieldName("context"), resultContext);
        action.execute(env);
        verify(responseAction, times(1)).execute(env);
    }
}
