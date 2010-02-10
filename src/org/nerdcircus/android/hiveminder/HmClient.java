package org.nerdcircus.android.hiveminder;

import org.nerdcircus.android.hiveminder.HmAuthException;
import org.nerdcircus.android.hiveminder.HmParseException;
import org.nerdcircus.android.hiveminder.parser.HmXmlParser;
import org.nerdcircus.android.hiveminder.model.*;

import android.content.SharedPreferences;
import android.content.Context;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;

import android.util.Log;
import java.util.Map;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.InputStream;


import org.apache.http.StatusLine;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.cookie.Cookie;
import org.apache.http.HeaderIterator;
import org.apache.http.Header;
import org.apache.http.HttpVersion;

//from httpmime
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;

public class HmClient implements WebActivityController {

    //AsyncTask subclass for braindump.
    private class BraindumpTask extends AsyncTask<String, Void, HmResponse>{
        protected HmResponse doInBackground(String... t){
            try {
                //only use the first arg.
                return doBraindump(t[0]);
            }
            catch(HmAuthException e){
                // not logged in. oops.
                return null;
            }
        }
        protected void onPostExecute(HmResponse resp){
            if( null == resp){
                Log.d(TAG, "null response. should i launch login?");
                //FIXME: there's a few ways we can end up here, but for now,
                // assume we're not logged in.(maybe encapsulate exception in the response?)
                
                // Hide the dialog..
                Message m = Message.obtain();
                m.what = ProgressDialogHandler.WHAT_HIDE_PROGRESS;
                sendUiMessage(m);
                // and launch login..
                m = Message.obtain();
                m.what = ProgressDialogHandler.WHAT_SHOW_LOGIN;
                sendUiMessage(m);
                return;
            }
            // Hide the dialog..
            Message m = Message.obtain();
            m.what = ProgressDialogHandler.WHAT_HIDE_PROGRESS;
            sendUiMessage(m);
            // and toast.
            m = Message.obtain();
            m.what = ProgressDialogHandler.WHAT_SHOW_RESULT_DIALOG;
            m.obj = resp;
            sendUiMessage(m);
        }
    }

    //AsyncTask subclass for TaskSearch.
    //TODO: this class name sucks.
    //TODO: should be generic. - is that possible?
    private class TaskSearchTask extends AsyncTask<String, Void, HmResponse>{

        protected HmResponse doInBackground(String... t){
            Log.d(TAG, "searching for: " + t[0]);
            try {
                if( "".equals(t[0]) ){
                    MultipartEntity defaultSearch = new MultipartEntity();
                    defaultSearch.addPart("complete_not", new StringBody("1"));
                    defaultSearch.addPart("accepted", new StringBody("1"));
                    defaultSearch.addPart("owner", new StringBody("me"));
                    defaultSearch.addPart("starts_before", new StringBody("tomorrow"));
                    defaultSearch.addPart("depends_on_count", new StringBody("0"));
                    Log.d(TAG, "no search terms provided. using default values");
                    return doAction("TaskSearch", defaultSearch);
                } else {
                    MultipartEntity post_data = new MultipartEntity();
                    post_data.addPart("query", new StringBody(t[0]));
                    if( ! mPrefs.getBoolean("show_completed_tasks", false)){
                        Log.d(TAG, "excluding completed tasks");
                        post_data.addPart("complete_not", new StringBody("1"));
                    }
                    return doAction("TaskSearch", post_data);
                }
            }
            catch (UnsupportedEncodingException e) {
                Log.d(TAG, e.toString());
            }
            catch (IOException e) {
                Log.d(TAG, e.toString());
            }
            catch(HmAuthException e){
                // not logged in. oops.
                Log.d(TAG, e.toString());
            }
            return null;
        }

        protected void onPostExecute(HmResponse resp){
            if( null == resp){
                Log.d(TAG, "null response. should i launch login?");
                //FIXME: there's a few ways we can end up here, but for now,
                // assume we're not logged in.(maybe encapsulate exception in the response?)
                
                // Hide the dialog..
                Message m = Message.obtain();
                m.what = ProgressDialogHandler.WHAT_HIDE_PROGRESS;
                sendUiMessage(m);
                // and launch login..
                m = Message.obtain();
                m.what = ProgressDialogHandler.WHAT_SHOW_LOGIN;
                sendUiMessage(m);
                return;
            }
            // Hide the dialog..
            Message m = Message.obtain();
            m.what = ProgressDialogHandler.WHAT_HIDE_PROGRESS;
            sendUiMessage(m);
            // and toast.
            //TODO: this is not the right action for a task search.
            m = Message.obtain();
            m.what = ProgressDialogHandler.WHAT_SEARCH_COMPLETE;
            m.obj = resp;
            sendUiMessage(m);
        }
    }

    private String TAG = "HmClient";
    public boolean DEBUG = true;

    private String MONIKER = "hivedroid";
    private String BASEURL = "http://hiveminder.com/";

    private SharedPreferences mPrefs;
    private DefaultHttpClient mHttpClient;

    private boolean mIsLoggedIn = false;
    private boolean mDialogVisible = false;

    //Keep a reference to our controlling activity
    private WebActivity mActivity;
    private Handler mHandler;

    public HmClient(Activity activity){
        mPrefs = activity.getSharedPreferences("auth", Context.MODE_PRIVATE);
        mHttpClient = new DefaultHttpClient();

        if( ! mPrefs.getString("auth_cookie", "").equals("")){
            Log.d(TAG, "stored sid cookie: "+mPrefs.getString("auth_cookie", ""));
            setSidCookie(mPrefs.getString("auth_cookie", ""));
        }
    }

    // used when this hivemind is passed around via onRetainLastNonConfigurationInstance()
    public void setActivity(WebActivity a){
        this.mActivity = a;
    }
    public WebActivity getActivity(){
        return this.mActivity;
    }
    public void setUiHandler(Handler a){
        this.mHandler = a;
    }
    public Handler getUiHandler(){
        return this.mHandler;
    }

    protected void reloadSidCookie(){
        if( mPrefs.contains("auth_cookie") ){
            setSidCookie(mPrefs.getString("auth_cookie", ""));
        }
    }

    /** set the session ID cookie for Hiveminder.
     */
    private void setSidCookie(String cookie){
        BasicClientCookie c = new BasicClientCookie("JIFTY_SID_HIVEMINDER", cookie);
        c.setDomain(".hiveminder.com");
        c.setPath("/");
        mHttpClient.getCookieStore().addCookie(c);
    }
    // save this cookie in our prefs.
    public void saveSidCookie(){
        for( Cookie c : mHttpClient.getCookieStore().getCookies()){
            if("JIFTY_SID_HIVEMINDER".equals(c.getName())){
                Log.d(TAG, "saving sid cookie: "+ c.getValue());
                mPrefs.edit().putString("auth_cookie", c.getValue()).commit();
            }
        }
    }

    public void setSsl(boolean ssl){
        if(ssl){
            BASEURL = "https://hiveminder.com/";
        }
        else {
            BASEURL = "http://hiveminder.com/";
        }
    }

    private void debugCookies(){
        Log.d(TAG, "Cookies: ");
        for( Cookie c : mHttpClient.getCookieStore().getCookies()){
            Log.d(TAG, c.toString());
        }
    }
    /** Braindump some text to HM, and have tasks be created from it
     * this function is used by the AsyncTask subclass above.
     */
    public HmResponse doBraindump(String text) throws HmAuthException {
        try {
            MultipartEntity post_data = new MultipartEntity();
            post_data.addPart("text", new StringBody(text));
            return doAction("ParseTasksMagically", post_data, true);
        }
        catch (UnsupportedEncodingException e) {
            Log.d(TAG, e.toString());
        }
        catch (IOException e) {
            Log.d(TAG, e.toString());
        }
        return null;
    }

    // Call our AsyncTask subclass to do all the real work here.
    public HmResponse doBraindumpAsyncTask(String text) throws HmAuthException {
        new BraindumpTask().execute(text);
        return null;
    }

    public HmResponse doSearchAsyncTask(String text) throws HmAuthException {
        new TaskSearchTask().execute(text);
        return null;
    }

    /*
     * Backward-compatible version that assumes you dont want fast parsing.
     * */
    public HmResponse doAction(String action, MultipartEntity post_data) throws HmAuthException {
        return doAction(action, post_data, false);
    }

    public HmResponse doAction(String action, MultipartEntity post_data, boolean fast_parse) throws HmAuthException {
        try {
            String urlString = BASEURL + "/=/action/BTDT.Action." + action + ".xml";
            Log.d(TAG, "attempting to call: " + urlString);
            HttpPost p = new HttpPost(urlString);
            p.setEntity(post_data);
            debugCookies();
            Log.d(TAG, "sending request");
            HttpResponse resp = mHttpClient.execute(p);
            Log.d(TAG, "got response");
            debugCookies();
            saveSidCookie();
            Log.d(TAG, "passing to parser..");
            // do a "lazy" parse if fast_parse is set to true.
            InputStream instream = resp.getEntity().getContent();
            HmResponse r = new HmXmlParser(instream).parse(fast_parse);
            instream.close();
            resp.getEntity().consumeContent(); //enable reuse of this connection.
            p.abort(); //XXX: how about now!?

            Log.d(TAG, "done");
            if(r.getSuccess()){
                Log.d(TAG, "Success!");
            }
            return r;
        }
        catch (UnsupportedEncodingException e) {
            Log.d(TAG, e.toString());
        }
        catch (IOException e) {
            Log.d(TAG, e.toString());
        }
        catch (HmParseException e) {
            Log.d(TAG, e.toString());
        }
        return null;
    }

    /** Log in, get session cookie.
     */
    public HmResponse doLogin(String address, String password){
        try {
            /* This is madness.
             * the "regular" xml api doesnt do login properly, despite being a
             * valid action.  so, we have to use the old-n-busted jifty stuff,
             * instead of the new hotness.
             * update: according to kevin, this is fixed now.
             */
            HttpPost p = new HttpPost(BASEURL + "/__jifty/webservices/xml");
            
            MultipartEntity me = new MultipartEntity();

            // old-style argument passing. ugly.
            me.addPart("J:A-"+ MONIKER, new StringBody("Login"));
            me.addPart("J:A:F-address-" + MONIKER, new StringBody(address));
            me.addPart("J:A:F-password-" + MONIKER, new StringBody(password));

            p.setEntity(me);
            debugCookies();
            Log.d(TAG, "sending request");
            HttpResponse resp = mHttpClient.execute(p);
            Log.d(TAG, "got response");
            debugCookies();
            saveSidCookie();
            Log.d(TAG, "passing to parser..");
            HmResponse r = new HmXmlParser(resp.getEntity().getContent()).parse();
            Log.d(TAG, "done");
            if(r.getSuccess()){
                Log.d(TAG, "Success!");
            }
            return r;
        }
        catch (UnsupportedEncodingException e) {
            Log.d(TAG, e.toString());
        }
        catch (IOException e) {
            Log.d(TAG, e.toString());
        }
        catch (HmParseException e) {
            Log.d(TAG, e.toString());
        }
        catch (HmAuthException e) {
            Log.d(TAG, "Auth failed. should never happen: " + e.toString());
        }
        return null;
    }


    /* Send a message to the Ui thread, via the activity's handler.
     *      */
    public boolean sendUiMessage(Message m){
        return this.getUiHandler().sendMessage(m);
    }


}

