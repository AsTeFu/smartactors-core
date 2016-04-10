package info.smart_tools.smartactors.core.recursive_scope;

import info.smart_tools.smartactors.core.iscope.IScope;
import info.smart_tools.smartactors.core.iscope.exception.ScopeException;
import org.junit.Test;
import static org.junit.Assert.*;

public class RecursiveScopeTest {

    @Test
    public void checkCreation() {
        IScope scope = new Scope(null);
        assertNotNull(scope);
    }

    @Test
    public void checkCreationWithParent() {
        IScope parent = new Scope(null);
        assertNotNull(parent);
        IScope child = new Scope(parent);
        assertNotNull(child);
    }

    @Test
    public void checkStoringAndGettingValue() {
        IScope scope = new Scope(null);
        Integer number = 1;
        scope.setValue("number", number);
        assertEquals(scope.getValue("number"), number);
    }

    @Test(expected = ScopeException.class)
    public void checkGettingAbsentValue() throws ScopeException {
        IScope scope = new Scope(null);
        scope.getValue("number");
    }

    @Test
    public void checkRecursiveLogic() {
        IScope parent = new Scope(null);
        assertNotNull(parent);
        IScope child = new Scope(parent);
        assertNotNull(child);
        Integer number = 1;
        parent.setValue("number", number);
        assertEquals(child.getValue("number"), number);
    }

    @Test(expected = ScopeException.class)
    public void checkValueDeletion() {
        IScope scope = new Scope(null);
        Integer number = 1;
        scope.setValue("number", number);
        assertEquals(scope.getValue("number"), number);
        scope.deleteValue("number");
        scope.getValue("number");
    }

    @Test
    public void checkAbsentValueDeletion() {
        IScope scope = new Scope(null);
        scope.deleteValue("number");
    }

    @Test
    public void checkNullKeyUsage() {
        IScope scope = new Scope(null);
        Integer number = 1;
        scope.setValue(null, number);
        assertEquals(scope.getValue(null), number);
    }

    @Test
    public void checkEqualKeysUsage() {
        IScope scope = new Scope(null);
        Integer number1 = 1;
        Integer number2 = 2;
        scope.setValue("number", number1);
        scope.setValue("number", number2);
        assertEquals(scope.getValue("number"), number2);
    }
}
