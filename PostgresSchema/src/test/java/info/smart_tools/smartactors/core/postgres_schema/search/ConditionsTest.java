package info.smart_tools.smartactors.core.postgres_schema.search;

import info.smart_tools.smartactors.core.db_storage.exceptions.QueryBuildException;
import info.smart_tools.smartactors.core.db_storage.interfaces.SQLQueryParameterSetter;
import info.smart_tools.smartactors.core.ds_object.DSObject;
import info.smart_tools.smartactors.core.invalid_argument_exception.InvalidArgumentException;
import info.smart_tools.smartactors.core.postgres_connection.QueryStatement;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Test for Conditions
 */
public class ConditionsTest {

    private StringWriter body;
    private QueryStatement query;
    private QueryConditionWriter writer;
    private QueryConditionWriterResolver resolver;
    private FieldPath fieldPath;
    private Object queryParameter;
    private List<SQLQueryParameterSetter> setters;

    @Before
    public void setUp() throws QueryBuildException {
        body = new StringWriter();

        query = mock(QueryStatement.class);
        when(query.getBodyWriter()).thenReturn(body);

        writer = mock(QueryConditionWriter.class);
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                String resolver = String.valueOf(args[1]);
                String path = String.valueOf(args[2]);
                String param = String.valueOf(args[3]);
                List setters = (List) args[4];
                setters.add(param);
                body.write(String.format(" %s, %s, %s, %s ", resolver, path, param, setters));
                return null;
            }}).when(writer).write(same(query), any(), any(), any(), any());

        resolver = mock(QueryConditionWriterResolver.class);
        when(resolver.resolve(any())).thenReturn(writer);
        when(resolver.toString()).thenReturn("resolver");

        fieldPath = mock(FieldPath.class);
        when(fieldPath.toString()).thenReturn("fieldPath");

        setters = new ArrayList<>();
    }

    @Test
    public void testAndOnIObject() throws QueryBuildException, InvalidArgumentException {
        queryParameter = new DSObject("{ \"a\": \"b\", \"c\": \"d\" }");
        Conditions.writeAndCondition(query, resolver, fieldPath, queryParameter, setters);
        assertEquals("( resolver, fieldPath, b, [b] AND resolver, fieldPath, d, [b, d] )", body.toString());
    }

    @Test
    public void testAndOnList() throws QueryBuildException, InvalidArgumentException {
        queryParameter = new ArrayList() {{ add("a"); add("b"); }};
        Conditions.writeAndCondition(query, resolver, fieldPath, queryParameter, setters);
        assertEquals("( resolver, fieldPath, a, [a] AND resolver, fieldPath, b, [a, b] )", body.toString());
    }

    @Test
    public void testOrOnIObject() throws QueryBuildException, InvalidArgumentException {
        queryParameter = new DSObject("{ \"a\": \"b\", \"c\": \"d\" }");
        Conditions.writeOrCondition(query, resolver, fieldPath, queryParameter, setters);
        assertEquals("( resolver, fieldPath, b, [b] OR resolver, fieldPath, d, [b, d] )", body.toString());
    }

    @Test
    public void testOrOnList() throws QueryBuildException, InvalidArgumentException {
        queryParameter = new ArrayList() {{ add("a"); add("b"); }};
        Conditions.writeOrCondition(query, resolver, fieldPath, queryParameter, setters);
        assertEquals("( resolver, fieldPath, a, [a] OR resolver, fieldPath, b, [a, b] )", body.toString());
    }

    @Test
    public void testNotOnIObject() throws QueryBuildException, InvalidArgumentException {
        queryParameter = new DSObject("{ \"a\": \"b\", \"c\": \"d\" }");
        Conditions.writeNotCondition(query, resolver, fieldPath, queryParameter, setters);
        assertEquals("(NOT( resolver, fieldPath, b, [b] AND resolver, fieldPath, d, [b, d] ))", body.toString());
    }

    @Test
    public void testNotOnList() throws QueryBuildException, InvalidArgumentException {
        queryParameter = new ArrayList() {{ add("a"); add("b"); }};
        Conditions.writeNotCondition(query, resolver, fieldPath, queryParameter, setters);
        assertEquals("(NOT( resolver, fieldPath, a, [a] AND resolver, fieldPath, b, [a, b] ))", body.toString());
    }

}