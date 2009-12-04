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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.Runnable;
import java.util.List;
import java.util.ArrayList;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

import org.nerdcircus.android.hiveminder.HmClient;
import org.nerdcircus.android.hiveminder.model.HmResponse;
import org.nerdcircus.android.hiveminder.ProgressDialogHandler;
class TaskListActivity extends ListActivity {
    public static int DIALOG_PROGRESS = 1;

    private HmClient mHivemind;

    public onCreate(Bundle icicle){
        super.onCreate(icicle);
        setHandler(new ProgressDialogHandler(this));

        if (getLastNonConfigurationInstance() != null){
            Log.d(TAG, "re-using saved hivemind!");
            mHivemind = (HmClient)getLastNonConfigurationInstance();
            mHivemind.setActivity(this);
        }
        else {
            //no saved instance. make a fresh one.
            Log.d(TAG, "no saved hivemind. making a new one");
            mHivemind = new HmClient(this);
            //use ssl, if prefs say so.
            //mHivemind.setSsl(getPreferences(Context.MODE_PRIVATE).getBoolean("use_ssl", false));
        }

        //XXX do a search, and show the results.
        showDialog(DIALOG_PROGRESS);

        ListAdapter a = new ArrayAdapter(this, R.layout.taskitem, R.id.tasktext);
        setListAdapter(a);
    }

    @Override
    public Object onRetainNonConfigurationInstance(){
        return mHivemind;
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
    }

}
