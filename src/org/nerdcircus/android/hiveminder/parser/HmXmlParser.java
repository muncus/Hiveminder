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

    public XmlPullParser xpp;

    public boolean DEBUG = false;

    public HmXmlParser() throws HmParseException {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
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
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_DOCUMENT && DEBUG) {
                   Log.d(TAG, "Start document");
                } else if(eventType == XmlPullParser.END_DOCUMENT && DEBUG) {
                    Log.d(TAG, "End document");
                } else if(eventType == XmlPullParser.START_TAG) {
                    if(DEBUG){
                        Log.d(TAG, "Start tag "+xpp.getName());
                    }
                    if( "html".equals(xpp.getName())){
                        //This is not the page we're looking for...
                        throw new HmAuthException();
                    }
                    if( xpp.getName().equals("action_class")){
                        xpp.next(); //skip to the text...
                        r.setAction(xpp.getText());
                    }
                    // most task-listing actions use the "tasks" tag name.
                    if( "tasks".equals(xpp.getName()) ){
                        Log.d(TAG, "Found task list...");
                        r.addTask(parseTask());
                    }
                    // but not ParseTasksMagically (aka- braindump)
                    if( "created".equals(xpp.getName()) ){
                        Log.d(TAG, "found list of Braindump tasks...");
                        //this is a list of tasks...
                        if( fast_parse == false ){
                            r.addTask(parseTask());
                        }
                    }
                    if( "message".equals(xpp.getName()) ){
                        xpp.next();
                        r.setMessage(xpp.getText());
                    }
                    if( "success".equals(xpp.getName()) ){
                        xpp.next();
                        r.setSuccess("1".equals(xpp.getText()));
                    }
                } else if(eventType == XmlPullParser.END_TAG && DEBUG) {
                    Log.d(TAG, "End tag "+xpp.getName());
                } else if(eventType == XmlPullParser.TEXT) {
                    if(DEBUG){
                        Log.d(TAG, "Text "+xpp.getText());
                    }
                    if("403 Forbidden".equals(xpp.getText())){
                        Log.d(TAG, "not logged in. excepting.");
                        throw new HmAuthException();
                    }
                }
                eventType = xpp.next();
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
        Log.d(TAG, "my closing tag will be: " + closingTagName);
        Task t = new Task();
        int eventType = xpp.getEventType();
        while( true ){
            // exit condition:
            if( eventType == XmlPullParser.END_TAG && closingTagName.equals(xpp.getName()) ){
                break;
            }
            String tag = xpp.getName();
            if(eventType == XmlPullParser.START_TAG){
                if(DEBUG){
                    Log.d(TAG, "start: " + xpp.getName());
                }
                //TODO: replace raw accesses with setters.
                if("id".equals(xpp.getName())){
                    Log.d(TAG, "found id tag");
                    xpp.next(); //skip to the text
                    t.id = new Long(xpp.getText()).longValue();
                }
                if("summary".equals(xpp.getName())){
                    Log.d(TAG, "found summary tag");
                    xpp.next(); //skip to the text
                    t.summary = xpp.getText();
                }
                if("complete".equals(xpp.getName())){
                    Log.d(TAG, "found complete tag");
                    xpp.next(); //skip to the text
                    if("1".equals(xpp.getText())){
                        Log.d(TAG, "complete is true!");
                        t.complete = true;
                    }
                    else { t.complete = false; }
                }
            }
            else if(eventType == XmlPullParser.START_DOCUMENT && DEBUG){
                Log.d(TAG, "start doc: " + xpp.getName());
            }
            else if(eventType == XmlPullParser.END_DOCUMENT && DEBUG){
                Log.d(TAG, "end doc: " + xpp.getName());
            }
            else if(eventType == XmlPullParser.TEXT && DEBUG){
                Log.d(TAG, "text: " + xpp.getText());
            }
            else if(eventType == XmlPullParser.END_TAG && DEBUG){
                Log.d(TAG, "end: " + xpp.getName());
            }
            else {
                //not a start tag..
                if(DEBUG){
                    Log.d(TAG, "not start: " + xpp.getName());
                }
            }
            eventType = xpp.next();
        }
        return t;
    }
}
