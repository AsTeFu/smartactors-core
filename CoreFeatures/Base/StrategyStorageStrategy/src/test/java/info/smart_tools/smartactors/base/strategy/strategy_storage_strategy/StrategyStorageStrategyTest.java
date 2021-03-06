package info.smart_tools.smartactors.base.strategy.strategy_storage_strategy;

import info.smart_tools.smartactors.base.exception.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.base.interfaces.i_addition_dependency_strategy.IAdditionDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.i_addition_dependency_strategy.exception.AdditionDependencyStrategyException;
import info.smart_tools.smartactors.base.interfaces.iaction.IAction;
import info.smart_tools.smartactors.base.interfaces.iaction.IBiFunction;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.IResolveDependencyStrategy;
import info.smart_tools.smartactors.base.interfaces.iresolve_dependency_strategy.exception.ResolveDependencyStrategyException;
import org.junit.Test;

import java.util.Map;
import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StrategyStorageStrategyTest {

    @Test
    public void checkInstanceCreation()
            throws Exception {
        IResolveDependencyStrategy sss = new StrategyStorageStrategy((a) -> a, (c, a) -> a);
        assertNotNull(sss);
    }

    @Test
    public void checkResolution()
            throws Exception {
        String value1 = "result1";
        String value2 = "result2";
        String defaultValue = "default";
        IResolveDependencyStrategy innerStrategy1 = mock(IResolveDependencyStrategy.class);
        IResolveDependencyStrategy innerStrategy2 = mock(IResolveDependencyStrategy.class);
        IResolveDependencyStrategy defaultStrategy = mock(IResolveDependencyStrategy.class);
        when(innerStrategy1.resolve(new Object[] {"in_something", "arg"})).thenReturn(value1);
        when(innerStrategy2.resolve(new Object[] {"out_something", "arg"})).thenReturn(value2);
        when(defaultStrategy.resolve(new Object[] {"something", "arg"})).thenReturn(defaultValue);
        when(defaultStrategy.resolve(new Object[] {"in_something", "arg"})).thenReturn(defaultValue);

        IBiFunction findValueByArgument = (map, arg) -> {
            char[] symbols = arg.toString().toCharArray();
            String defaultKey = "default";
            IResolveDependencyStrategy strategy = null;
            StringBuilder key = new StringBuilder();
            for (char c : symbols) {
                key.append(c);
                strategy = ((Map<String, IResolveDependencyStrategy>)map).get(key.toString());
                if (null != strategy) {
                    break;
                }
            }
            return null != strategy ? strategy : ((Map<String, IResolveDependencyStrategy>)map).get(defaultKey);
        };
        IResolveDependencyStrategy sss = new StrategyStorageStrategy((a) -> a, findValueByArgument);
        ((IAdditionDependencyStrategy) sss).register("in_", innerStrategy1);
        ((IAdditionDependencyStrategy) sss).register("out_", innerStrategy2);
        ((IAdditionDependencyStrategy) sss).register("default", defaultStrategy);


        String result = sss.resolve("in_something", "arg");
        assertEquals(value1, result);
        result = sss.resolve("out_something", "arg");
        assertEquals(value2, result);
        result = sss.resolve("something", "arg");
        assertEquals(defaultValue, result);

        ((IAdditionDependencyStrategy) sss).remove("in_");

        result = sss.resolve("in_something", "arg");
        assertEquals(defaultValue, result);
    }

    @Test (expected = ResolveDependencyStrategyException.class)
    public void checkResolutionException()
            throws Exception {
        IResolveDependencyStrategy sss = new StrategyStorageStrategy(null, null);
        sss.resolve(null);
    }

    @Test (expected = AdditionDependencyStrategyException.class)
    public void checkExceptionOnRegister()
            throws Exception {
        IResolveDependencyStrategy strategy = new StrategyStorageStrategy(
                (a) -> {
                    throw new InvalidArgumentException("");
                },
                null
        );
        ((IAdditionDependencyStrategy) strategy).register(null, null);
    }

    @Test (expected = AdditionDependencyStrategyException.class)
    public void checkExceptionOnRemove()
            throws Exception {
        IResolveDependencyStrategy strategy = new StrategyStorageStrategy(
                (a) -> {
                    throw new InvalidArgumentException("");
                },
                null
        );
        ((IAdditionDependencyStrategy) strategy).remove(null);
    }
}
