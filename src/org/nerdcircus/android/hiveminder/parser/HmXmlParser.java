package org.nerdcircus.android.hiveminder.parser;

import java.io.IOException;
import java.io.StringReader;
import java.io.Reader;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

import org.nerdcircus.android.hiveminder.HmParseException;
import org.nerdcircus.android.hiveminder.HmAuthException;
import org.nerdcircus.android.hiveminder.model.Task;
import org.nerdcircus.android.hiveminder.model.HmResponse;

/** class to handle the parsing of an XML feed from hiveminder's xml api.
 */
public class HmXmlParser {

    public static String TAG = "HmXmlParser";
    public static boolean DEBUG = false;

    public XmlPullParser xpp;


    public HmXmlParser() throws HmParseException {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setValidating(false); // dont validate.
            factory.setNamespaceAware(false); //no namespaces.
            this.xpp = factory.newPullParser();
        }
        catch (XmlPullParserException e){
            throw new HmParseException(e);
        }
    }

    /* convenience constructor which calls xpp.setInput() */
    public HmXmlParser(InputStream is) throws HmParseException {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            this.xpp = factory.newPullParser();
            xpp.setInput(is, null);
        }
        catch (XmlPullParserException e){
            throw new HmParseException(e);
        }
    }

    public HmXmlParser(Reader in) throws HmParseException {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            this.xpp = factory.newPullParser();
            xpp.setInput(in);
        }
        catch (XmlPullParserException e){
            throw new HmParseException(e);
        }
    }

    /* parse, by default, will not skip processing the task items. */
    public HmResponse parse() throws HmAuthException, HmParseException {
        return parse(false);
    }

    /** parse a hiveminder call into an HmResponse object.
     * the fast_parse argument will skip parsing the tasks contained in the result, and only
     * return the success/message/error fields.
     */
    public HmResponse parse(boolean fast_parse) throws HmAuthException, HmParseException {
        try {
            // do the event loop here.
            HmResponse r = new HmResponse();
            int eventType = xpp.getEventType();
            DebugLog(TAG, "first event type: " + eventType);
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_DOCUMENT ) {
                    DebugLog(TAG, "Start document");
                    //XXX: NOTE: advancing the parser. beware!
                    eventType = xpp.next();
                    if (eventType == XmlPullParser.TEXT){
                        Log.d(TAG, "text after start_document. this isnt xml.");
                        throw new HmAuthException("non-xml content.");
                    }
                } else if(eventType == XmlPullParser.END_DOCUMENT ) {
                    DebugLog(TAG, "End document");
                } else if(eventType == XmlPullParser.START_TAG) {
                    DebugLog(TAG, "Start tag "+xpp.getName());
                    if( "html".equals(xpp.getName())){
                        //This is not the page we're looking for...
                        Log.d(TAG, "html. must be auth exception!");
                        throw new HmAuthException("html element found. probably the splash screen");
                    }
                    if( xpp.getName().equals("action_class")){
                        DebugLog(TAG, "found action_class");
                        xpp.next(); //skip to the text...
                        r.setAction(xpp.getText());
                    }
                    // most task-listing actions use the "tasks" tag name.
                    if( "tasks".equals(xpp.getName()) ){
                        DebugLog(TAG, "Found task list...");
                        r.addTask(parseTask());
                    }
                    // but not ParseTasksMagically (aka- braindump)
                    if( "created".equals(xpp.getName()) ){
                        DebugLog(TAG, "found list of Braindump tasks...");
                        //this is a list of tasks...
                        if( fast_parse == false ){
                            r.addTask(parseTask());
                        }
                    }
                    if( "message".equals(xpp.getName()) ){
                        DebugLog(TAG, "found message");
                        xpp.next();
                        r.setMessage(xpp.getText());
                    }
                    if( "success".equals(xpp.getName()) ){
                        DebugLog(TAG, "found success");
                        xpp.next();
                        r.setSuccess("1".equals(xpp.getText()));
                    }
                    else {
                        DebugLog(TAG, "Tag i dont care about:" + xpp.getName());
                    }
                } else if(eventType == XmlPullParser.END_TAG ) {
                    DebugLog(TAG, "End tag "+xpp.getName());
                } else if(eventType == XmlPullParser.TEXT) {
                    DebugLog(TAG, "Text "+xpp.getText());
                    //NB: this doesnt appear to happen anymore
                    if("403 Forbidden".equals(xpp.getText())){
                        Log.d(TAG, "not logged in. excepting.");
                        throw new HmAuthException();
                    }
                }
                eventType = xpp.next();
                Log.d(TAG, "event type: " + eventType);
            }

            if(r == new HmResponse() ){
                Log.d(TAG, "failed to parse");
                throw new HmAuthException();
            }

            return r;
        }
        catch (XmlPullParserException e){
            throw new HmParseException(e);
        }
        catch (IOException e){
            throw new HmParseException(e);
        }
    }

    /** parses a task from the current location of our xpp */
    public Task parseTask() throws XmlPullParserException, IOException {
        // this tag name indicates when we should stop parsing.
        // this is needed because tasks are enclosed in one of several tags.
        String closingTagName = xpp.getName();
        DebugLog(TAG, "my closing tag will be: " + closingTagName);
        Task t = new Task();
        int eventType = xpp.getEventType();
        while( true ){
            // exit condition:
            if( eventType == XmlPullParser.END_TAG && closingTagName.equals(xpp.getName()) ){
                break;
            }
            String tag = xpp.getName();
            if(eventType == XmlPullParser.START_TAG){
                DebugLog(TAG, "start: " + xpp.getName());
                //TODO: replace raw accesses with setters.
                if("id".equals(xpp.getName())){
                    DebugLog(TAG, "found id tag");
                    xpp.next(); //skip to the text
                    t.id = new Long(xpp.getText()).longValue();
                }
                if("summary".equals(xpp.getName())){
                    DebugLog(TAG, "found summary tag");
                    xpp.next(); //skip to the text
                    t.summary = xpp.getText();
                    DebugLog(TAG, "Task: " + t.summary);
                }
                if("complete".equals(xpp.getName())){
                    DebugLog(TAG, "found complete tag");
                    xpp.next(); //skip to the text
                    if("1".equals(xpp.getText())){
                        DebugLog(TAG, "complete is true!");
                        t.complete = true;
                    }
                    else { t.complete = false; }
                }
            }
            else if(eventType == XmlPullParser.START_DOCUMENT){
                DebugLog(TAG, "start doc: " + xpp.getName());
            }
            else if(eventType == XmlPullParser.END_DOCUMENT){
                DebugLog(TAG, "end doc: " + xpp.getName());
            }
            else if(eventType == XmlPullParser.TEXT){
                DebugLog(TAG, "text: " + xpp.getText());
            }
            else if(eventType == XmlPullParser.END_TAG){
                DebugLog(TAG, "end: " + xpp.getName());
            }
            else {
                //not a start tag..
                DebugLog(TAG, "not start: " + xpp.getName());
            }
            eventType = xpp.next();
        }
        return t;
    }

    void DebugLog(String tag, String text){
        if( ! DEBUG) return;
        Log.d(tag, text);
    }
}
