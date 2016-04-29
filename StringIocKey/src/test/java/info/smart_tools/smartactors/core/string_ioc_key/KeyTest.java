package info.smart_tools.smartactors.core.string_ioc_key;

import info.smart_tools.smartactors.core.ioc.IKey;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for Key
 */
public class KeyTest {

    static final class Fixture {

        static Key x = new Key("abc");
        static Key y = new Key("abc");
        static Key z = new Key("abc");
        static Key notx = new Key("a");

        static Key xInt = new Key<Integer>(Integer.class, "abc");
        static Key yInt = new Key<Integer>(Integer.class, "abc");
        static Key zInt = new Key<Integer>(Integer.class, "abc");
        static Key notxInt = new Key<Integer>(Integer.class, "a");
    }

    @Test
    public void testEqual_ToSelf() {
        assertTrue("Class equal to itself.", Fixture.x.equals(Fixture.x));
        assertTrue("Class equal to itself.", Fixture.xInt.equals(Fixture.xInt));
    }

    @Test
    public void testPassIncompatibleType_isFalse() {
        assertFalse("Passing incompatible object to equals should return false", Fixture.x.equals(Fixture.notx));
        assertFalse("Passing incompatible object to equals should return false", Fixture.xInt.equals(Fixture.notxInt));
    }

    @Test
    public void testNullReference_isFalse() {
        assertFalse("Passing null to equals should return false", Fixture.x.equals(null));
        assertFalse("Passing null to equals should return false", Fixture.xInt.equals(null));
    }

    @Test
    public void testEquals_isReflexive_isSymmetric() {
        assertTrue("Reflexive test fail x,y", Fixture.x.equals(Fixture.y));
        assertTrue("Symmetric test fail y", Fixture.y.equals(Fixture.x));
        assertTrue("Reflexive test fail x,y", Fixture.xInt.equals(Fixture.yInt));
        assertTrue("Symmetric test fail y", Fixture.yInt.equals(Fixture.xInt));
    }

    @Test
    public void testEquals_isTransitive() {
        assertTrue("Transitive test fails x,y", Fixture.x.equals(Fixture.y));
        assertTrue("Transitive test fails y,z", Fixture.y.equals(Fixture.z));
        assertTrue("Transitive test fails x,z", Fixture.x.equals(Fixture.z));
        assertTrue("Transitive test fails x,y", Fixture.xInt.equals(Fixture.yInt));
        assertTrue("Transitive test fails y,z", Fixture.yInt.equals(Fixture.zInt));
        assertTrue("Transitive test fails x,z", Fixture.xInt.equals(Fixture.zInt));
    }

    @Test
    public void testEquals_isConsistent() {
        assertTrue("Consistent test fail x,y", Fixture.x.equals(Fixture.y));
        assertTrue("Consistent test fail x,y", Fixture.x.equals(Fixture.y));
        assertFalse(Fixture.notx.equals(Fixture.x));
        assertFalse(Fixture.notx.equals(Fixture.x));
        assertTrue("Consistent test fail x,y", Fixture.xInt.equals(Fixture.yInt));
        assertTrue("Consistent test fail x,y", Fixture.xInt.equals(Fixture.yInt));
        assertFalse(Fixture.notxInt.equals(Fixture.xInt));
        assertFalse(Fixture.notxInt.equals(Fixture.xInt));
    }

    @Test
    public void testHashcode_isConsistent() {
        int initialHashcode = Fixture.x.hashCode();
        assertEquals("Consistent hashcode test fails", initialHashcode, Fixture.x.hashCode());
        int initialHashcodeInt = Fixture.xInt.hashCode();
        assertEquals("Consistent hashcode test fails", initialHashcodeInt, Fixture.xInt.hashCode());
    }

    @Test
    public void testHashcode_twoEqualsObjects_produceSameNumber() {
        int xHashcode = Fixture.x.hashCode();
        int yHashcode = Fixture.y.hashCode();
        assertEquals("Equal object, return equal hashcode test fails", xHashcode, yHashcode);
        int xHashcodeInt = Fixture.xInt.hashCode();
        int yHashcodeInt = Fixture.yInt.hashCode();
        assertEquals("Equal object, return equal hashcode test fails", xHashcodeInt, yHashcodeInt);
    }

    @Test
    public void testHashcode_twoUnEqualObjects_produceDifferentNumber() {
        int xHashcode = Fixture.x.hashCode();
        int notxHashcode = Fixture.notx.hashCode();
        assertTrue("Equal object, return unequal hashcode test fails", !(xHashcode == notxHashcode));
        int xHashcodeInt = Fixture.xInt.hashCode();
        int notxHashcodeInt = Fixture.notxInt.hashCode();
        assertTrue("Equal object, return unequal hashcode test fails", !(xHashcodeInt == notxHashcodeInt));
    }

    @Test
    public void checkMapResolution() {
        Map<IKey<Integer>, Integer> map = new HashMap<IKey<Integer>, Integer>();
        Integer a = 1;
        Integer b = 2;
        map.put(Fixture.xInt, a);
        map.put(Fixture.yInt, b);

        Object resultA = map.get(Fixture.xInt);
        assertEquals(resultA.getClass(), Integer.class);
    }

    @Test
    public void checkMapResolutionWithSameKeys() {
        Map<IKey<Integer>, Integer> map = new HashMap<IKey<Integer>, Integer>();
        Integer a = 1;
        Integer b = 2;
        map.put(Fixture.xInt, a);
        map.put(Fixture.xInt, b);

        assertEquals(map.size(), 1);
        Object resultA = map.get(Fixture.xInt);
        assertEquals(resultA.getClass(), Integer.class);
        assertEquals(resultA, b);
    }
}
