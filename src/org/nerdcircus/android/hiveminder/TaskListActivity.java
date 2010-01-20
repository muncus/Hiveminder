/** Task List Activity - showsa listview of tasks
*/
package org.nerdcircus.android.hiveminder;

import android.app.ListActivity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Toast;
import android.util.Log;

import java.lang.Runnable;
import java.util.List;
import java.util.ArrayList;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

import org.nerdcircus.android.hiveminder.HmClient;
import org.nerdcircus.android.hiveminder.model.HmResponse;
import org.nerdcircus.android.hiveminder.model.Task;
import org.nerdcircus.android.hiveminder.HmAuthException;
import org.nerdcircus.android.hiveminder.ProgressDialogHandler;

public class TaskListActivity extends ListActivity {
    private String TAG = "TaskListActivity";

    public static int DIALOG_PROGRESS = 1;

    private HmClient mHivemind;
    private Handler mHandler;

    public void onCreate(Bundle icicle){
        super.onCreate(icicle);
        setHandler((Handler)new ProgressDialogHandler(this));

        if (getLastNonConfigurationInstance() != null){
            Log.d(TAG, "re-using saved hivemind!");
            mHivemind = (HmClient)getLastNonConfigurationInstance();
            mHivemind.setUiHandler(getHandler());
        }
        else {
            //no saved instance. make a fresh one.
            Log.d(TAG, "no saved hivemind. making a new one");
            mHivemind = new HmClient(this);
            mHivemind.setUiHandler(getHandler());
            //use ssl, if prefs say so.
            //mHivemind.setSsl(getPreferences(Context.MODE_PRIVATE).getBoolean("use_ssl", false));
        }

        //XXX do a search, and show the results.
        showDialog(DIALOG_PROGRESS);
        
        try {
            mHivemind.doSearchAsyncTask("klaxon");
        }
        catch (HmAuthException e){
            Log.e(TAG, "not logged in. boooooo. :(");
        }

        ListAdapter a = new ArrayAdapter(this, R.layout.taskitem, R.id.tasktext);
        setListAdapter(a);
    }

    @Override
    public Object onRetainNonConfigurationInstance(){
        return mHivemind;
    }

    // WebActivity interface methods.
    public Handler setHandler(Handler h){
        mHandler = h;
        return mHandler;
    }
    public Handler getHandler(){
        return mHandler;
    }
    public boolean sendUiMessage(Message m){
        return this.getHandler().sendMessage(m);
    }

    /* Dialog-related functions
     * for proper showing and hiding of dialogs.
     */
    protected Dialog onCreateDialog(int dialog_id){
        if( DIALOG_PROGRESS == dialog_id ){
            ProgressDialog d = new ProgressDialog(this);
            d.setTitle("Loading");
            d.setMessage("Fetching Tasks");
            d.setCancelable(true); //workaround for non-dismissing of dialog after orientation change.
            d.setIndeterminate(true);
            return d;
        }
        else {
            return super.onCreateDialog(dialog_id);
        }
    }

    // update listactivity with the list of tasks from r.
    public void showSearchResults(HmResponse r){
        Log.d(TAG, "Showing search results...");
        ArrayAdapter a = (ArrayAdapter)getListAdapter();
        a.clear();
        for (Task t : r.getTasks() )
            a.add(t.summary);
    }

}
