package info.smart_tools.smartactors.field_plugins.ifield_plugin;

import info.smart_tools.smartactors.feature_loading_system.bootstrap_item.BootstrapItem;
import info.smart_tools.smartactors.field.field.Field;
import info.smart_tools.smartactors.base.interfaces.iaction.IPoorAction;
import info.smart_tools.smartactors.base.interfaces.iaction.exception.ActionExecuteException;
import info.smart_tools.smartactors.feature_loading_system.interfaces.ibootstrap.IBootstrap;
import info.smart_tools.smartactors.iobject.ifield.IField;
import info.smart_tools.smartactors.iobject.ifield_name.IFieldName;
import info.smart_tools.smartactors.ioc.iioccontainer.exception.ResolutionException;
import info.smart_tools.smartactors.ioc.ikey.IKey;
import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.ioc.ioc.IOC;
import info.smart_tools.smartactors.feature_loading_system.interfaces.iplugin.exception.PluginException;
import info.smart_tools.smartactors.ioc.named_keys_storage.Keys;
import info.smart_tools.smartactors.ioc.resolve_by_name_ioc_with_lambda_strategy.ResolveByNameIocStrategy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.function.Function;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@PrepareForTest({IOC.class, Keys.class, ResolveByNameIocStrategy.class, IFieldPlugin.class, IFieldName.class, Function.class})
@RunWith(PowerMockRunner.class)
public class IFieldPluginTest {

    private IFieldPlugin plugin;
    private IBootstrap bootstrap;

    @Before
    public void setUp() throws Exception {

        mockStatic(IOC.class);
        mockStatic(Keys.class);

        bootstrap = mock(IBootstrap.class);
        plugin = new IFieldPlugin(bootstrap);
    }

    @Test
    public void ShouldCorrectLoadPlugin() throws Exception {

        BootstrapItem item = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments("IFieldPlugin").thenReturn(item);

        when(item.after("IOC")).thenReturn(item);
        when(item.after("IFieldNamePlugin")).thenReturn(item);

        plugin.load();

        verifyNew(BootstrapItem.class).withArguments("IFieldPlugin");

        verify(item).after("IOC");

        ArgumentCaptor<IPoorAction> iPoorActionArgumentCaptor = ArgumentCaptor.forClass(IPoorAction.class);
        verify(item).process(iPoorActionArgumentCaptor.capture());

        verify(bootstrap).add(item);

        IKey iFieldKey = mock(IKey.class);
        when(Keys.getOrAdd(IField.class.getCanonicalName())).thenReturn(iFieldKey);
        IKey iFieldNameKey = mock(IKey.class);
        when(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName")).thenReturn(iFieldNameKey);

        IFieldName fieldName = mock(IFieldName.class);
        when(IOC.resolve(eq(iFieldNameKey), anyString())).thenReturn(fieldName);

        ArgumentCaptor<ResolveByNameIocStrategy> resolveByNameIocStrategyArgumentCaptor =
            ArgumentCaptor.forClass(ResolveByNameIocStrategy.class);

        iPoorActionArgumentCaptor.getValue().execute();

        verifyStatic();
        Keys.getOrAdd(IField.class.getCanonicalName());

        verifyStatic();
        IOC.register(eq(iFieldKey), resolveByNameIocStrategyArgumentCaptor.capture());

        String fieldNameStr = "exampleField";
        Field field = mock(Field.class);
        whenNew(Field.class).withArguments(fieldName).thenReturn(field);

        assertTrue("Must return correct value",
            resolveByNameIocStrategyArgumentCaptor.getValue().resolve(fieldNameStr) == field);

        verifyNew(Field.class).withArguments(fieldName);

    }

    @Test
    public void ShouldInCorrectLoadPluginWhenNewBootstrapItemThrowException() throws Exception {


        whenNew(BootstrapItem.class).withArguments("IFieldPlugin").thenThrow(new InvalidArgumentException(""));

        try {
            plugin.load();
        } catch (PluginException e) {
            verifyNew(BootstrapItem.class).withArguments("IFieldPlugin");
            return;
        }
        fail();
    }

    @Test
    public void ShouldInCorrectExecuteActionWhenKeysThrowException() throws Exception {

        BootstrapItem item = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments("IFieldPlugin").thenReturn(item);

        when(item.after(anyString())).thenReturn(item);

        plugin.load();

        verifyNew(BootstrapItem.class).withArguments("IFieldPlugin");

        verify(item).after("IOC");

        ArgumentCaptor<IPoorAction> iPoorActionArgumentCaptor = ArgumentCaptor.forClass(IPoorAction.class);
        verify(item).process(iPoorActionArgumentCaptor.capture());

        verify(bootstrap).add(item);

        when(Keys.getOrAdd(IField.class.getCanonicalName())).thenThrow(new ResolutionException(""));

        try {
            iPoorActionArgumentCaptor.getValue().execute();
        } catch (ActionExecuteException e) {

            verifyStatic();
            Keys.getOrAdd(IField.class.getCanonicalName());
            return;
        }
        fail();

    }

    @Test
    public void MustInCorrectExecuteActionWhenNewCreateStrategyThrowException() throws Exception {

        BootstrapItem item = mock(BootstrapItem.class);
        whenNew(BootstrapItem.class).withArguments("IFieldPlugin").thenReturn(item);

        when(item.after(anyString())).thenReturn(item);

        plugin.load();

        verifyNew(BootstrapItem.class).withArguments("IFieldPlugin");

        verify(item).after("IOC");

        ArgumentCaptor<IPoorAction> iPoorActionArgumentCaptor = ArgumentCaptor.forClass(IPoorAction.class);
        verify(item).process(iPoorActionArgumentCaptor.capture());

        verify(bootstrap).add(item);

        IKey iFieldNameKey = mock(IKey.class);
        when(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName")).thenReturn(iFieldNameKey);
        IKey iFieldKey = mock(IKey.class);
        when(Keys.getOrAdd("info.smart_tools.smartactors.iobject.ifield_name.IFieldName")).thenReturn(iFieldKey);

        whenNew(ResolveByNameIocStrategy.class).withArguments(any()).thenThrow(new InvalidArgumentException(""));

        try {
            iPoorActionArgumentCaptor.getValue().execute();
        } catch (ActionExecuteException e) {

            verifyStatic();
            Keys.getOrAdd(IField.class.getCanonicalName());

            verifyNew(ResolveByNameIocStrategy.class).withArguments(any());
            return;
        }
        fail();

    }
}
