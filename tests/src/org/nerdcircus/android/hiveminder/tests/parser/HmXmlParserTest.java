package org.nerdcircus.android.hiveminder.tests.parser;

import junit.framework.TestCase;

import org.nerdcircus.android.hiveminder.parser.HmXmlParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.io.StringReader;

import org.nerdcircus.android.hiveminder.HmAuthException;
import org.nerdcircus.android.hiveminder.HmParseException;
import org.nerdcircus.android.hiveminder.model.*;

import android.test.InstrumentationTestRunner;
import android.util.Log;

public class HmXmlParserTest extends TestCase{

    private String TAG = "HmXmlParserTest";

    public void testCreate() throws HmParseException, XmlPullParserException, IOException {
        HmXmlParser x = new HmXmlParser();
        assertNotNull(x);
    }

    public void testTaskParse() throws HmParseException, XmlPullParserException, IOException {
        String task1 = "<task><id>1</id><summary>some content</summary></task>";
        HmXmlParser x = new HmXmlParser(new StringReader(task1));
        assertNotNull(x);

        x.xpp.next(); //skip the START_DOCUMENT event.
        Task t1 = x.parseTask();

        assertEquals("task ids equal", new Long("1").longValue(), t1.id);
        assertEquals("task content equal", "some content", t1.summary);
    }

    public void testParseBraindump() throws HmAuthException, HmParseException, XmlPullParserException, IOException {
        String task1 = "<data>"
            + "<action_class>BTDT::Action::ParseTasksMagically</action_class>"
            + "<content><created><id>1</id><summary>some content</summary></created></content>"
            + "<success>1</success>"
            + "<message>this is a message</message>"
            + "<data>";
        HmXmlParser x = new HmXmlParser(new StringReader(task1));
        assertNotNull(x);

        HmResponse r = x.parse();
        assertNotNull(r);
        assertTrue("success true", r.getSuccess());
        assertEquals("message set properly", "this is a message", r.getMessage());
        assertEquals("action set properly", "BTDT::Action::ParseTasksMagically", r.getAction());

        Task t1 = (Task)(r.getTasks()).get(0);
        assertEquals("task ids equal", new Long("1").longValue(), t1.id);
        assertEquals("task content equal", "some content", t1.summary);
    }

    public void testParseTaskSearch() throws HmAuthException, HmParseException, XmlPullParserException, IOException {
        String task1 = "<data><content>"
            + "<action_class>BTDT::Action::TaskSearch</action_class>"
            + "<tasks><id>1</id><summary>some content</summary></tasks>"
            + "<success>1</success>"
            + "<message>this is a message</message>"
            + "</content><data>";
        HmXmlParser x = new HmXmlParser(new StringReader(task1));
        assertNotNull(x);

        HmResponse r = x.parse();
        assertNotNull(r);
        assertTrue("success true", r.getSuccess());
        assertEquals("message set properly", "this is a message", r.getMessage());
        assertEquals("action set properly", "BTDT::Action::TaskSearch", r.getAction());

        Task t1 = (Task)(r.getTasks()).get(0);
        assertEquals("task ids equal", new Long("1").longValue(), t1.id);
        assertEquals("task content equal", "some content", t1.summary);
    }
}
