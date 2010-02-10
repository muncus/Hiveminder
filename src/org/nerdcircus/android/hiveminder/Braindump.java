package org.nerdcircus.android.hiveminder;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.net.Uri;
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

import android.util.Log;

public class Braindump extends Activity implements WebActivity
{
    private String BROWSER_TODO_URL = "http://hiveminder.com/mobile/list/not/complete/owner/me/starts/before/tomorrow/accepted/but_first/nothing";
    private String TAG = "Braindump";

    private HmClient mHivemind;
    private EditText mText;
    private Handler mHandler;
    private Activity mThis;
    private ProgressDialog mProgress;
    private boolean mDialogVisible;
    private SharedPreferences mPrefs = null;
     
    // Dialog Ids.
    public int DIALOG_PROGRESS = 1;

    public Handler getHandler(){
        return mHandler;
    }
    public Handler setHandler(Handler h){
        mHandler = h;
        return mHandler;
    }

    /** Called with the activity is first created. */
    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.braindump);
        setHandler(new ProgressDialogHandler(this));
        mThis = this;

        if (getLastNonConfigurationInstance() != null){
            Log.d(TAG, "re-using saved hivemind!");
            mHivemind = (HmClient)getLastNonConfigurationInstance();
            mHivemind.setUiHandler(this.getHandler());
        }
        else {
            //no saved instance. make a fresh one.
            Log.d(TAG, "no saved hivemind. making a new one");
            mHivemind = new HmClient(this);
            mHivemind.setUiHandler(this.getHandler());
            //use ssl, if prefs say so.
            mHivemind.setSsl(getPreferences(Context.MODE_PRIVATE).getBoolean("use_ssl", false));
        }

        mText = (EditText)findViewById(R.id.text);
        SharedPreferences mPrefs = getPreferences(Context.MODE_PRIVATE);

        //if this is our first run, prompt for authentication
        if(mPrefs.getBoolean("is_first_run", true)){
            mPrefs.edit().putBoolean("is_first_run", false).commit();
            Intent i = new Intent("org.nerdcircus.android.hiveminder.LOGIN");
            startActivity(i);
        }

        //restore saved braindump contents.
        if(mPrefs.contains("saved-braindump-text")){
            mText.setText(mPrefs.getString("saved-braindump-text", ""));
        }

        Button clearbtn = (Button)findViewById(R.id.clear);
        clearbtn.setOnClickListener( new Button.OnClickListener(){
            public void onClick(View v){
                clearTextField();
            }
        });

        Button dumpbutton = (Button)findViewById(R.id.dump);
        dumpbutton.setOnClickListener( new Button.OnClickListener(){
            public void onClick(View v){
                String text = mText.getText().toString();
                try{
                    saveBraindumpText(text);
                    dumpBrain(text);
                 }
                 catch(HmAuthException e){
                    Log.d(TAG, "not logged in. launching Login.");
                    Intent i = new Intent("org.nerdcircus.android.hiveminder.LOGIN");
                    startActivity(i);
                 }
            }
        });

    }

    protected void onPause(){
        super.onPause();
        saveBraindumpText(mText.getText().toString());
    }

    protected void onResume(){
        super.onResume();
        if(mPrefs == null){
            mPrefs = getPreferences(Context.MODE_PRIVATE);
        }
        //XXX: this function kept throwing NPEs here without the above.
        //seems like onCreate() is not called first?
        if(mPrefs.contains("saved-braindump-text")){
            mText.setText(mPrefs.getString("saved-braindump-text", ""));
            mText.selectAll(); //select it all, so we can erase if we want.
        }
        //in case we ran login, reload the sid cookie.
        mHivemind.reloadSidCookie();
    }

    public void dumpBrain(final String brain) throws HmAuthException{
        showDialog(1);
        //mHivemind.doThreadedBraindump(brain);
        mHivemind.doBraindumpAsyncTask(brain);
    }

    protected void saveBraindumpText(String s){
        mPrefs.edit().putString("saved-braindump-text", s).commit();
    }

    protected String getSavedBraindumpText(){
        return mPrefs.getString("saved-braindump-text", "");
    }

    protected void clearTextField(){
        mText.setText("");
        //and remove any saved text from our preferences.
        mPrefs.edit().remove("saved-braindump-text").commit();
    }

    public void onSaveInstanceState(Bundle state){
        super.onSaveInstanceState(state);
        state.putString("text", mText.getText().toString());
    }

    /* Dialog-related functions
     * for proper showing and hiding of dialogs.
     */
    protected Dialog onCreateDialog(int dialog_id){
        if( DIALOG_PROGRESS == dialog_id ){
            ProgressDialog d = new ProgressDialog(this);
            d.setTitle("Braindump");
            d.setMessage("Brain Dumping..");
            d.setCancelable(true); //workaround for non-dismissing of dialog after orientation change.
            d.setIndeterminate(true);
            return d;
        }
        else {
            return super.onCreateDialog(dialog_id);
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance(){
        return mHivemind;
    }

    public void onDestroy(){
        Log.d(TAG, "removing dialog from ondestroy");
        removeDialog(DIALOG_PROGRESS);
        super.onDestroy();
    }

    //Show a little menu, with a link to hiveminder.com/todo
    public boolean onCreateOptionsMenu(Menu menu){
        Intent hmlistIntent = new Intent("org.nerdcircus.android.hiveminder.SEARCH");
        hmlistIntent.putExtra(SearchManager.QUERY, ""); //XXX: trick, to get a null search.
        menu.add("Show ToDo List").setIntent(hmlistIntent);

        MenuItem mi = menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "Preferences");
        mi.setIcon(android.R.drawable.ic_menu_preferences);
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.setClassName(this, "org.nerdcircus.android.hiveminder.Preferences");
        mi.setIntent(i);

        return true;
    }
}
