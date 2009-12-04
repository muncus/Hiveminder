package org.nerdcircus.android.hiveminder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.widget.Toast;
import android.content.res.Configuration;

import java.lang.Runnable;
import java.util.List;
import java.util.ArrayList;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

import org.nerdcircus.android.hiveminder.HmClient;
import org.nerdcircus.android.hiveminder.model.HmResponse;

import android.util.Log;

public class ProgressDialogHandler extends Handler
{
    private String TAG = "ProgressDialogHandler";

    //Event types:
    public static int WHAT_SHOW_PROGRESS = 1;
    public static int WHAT_HIDE_PROGRESS = 2;
    public static int WHAT_SHOW_RESULT_DIALOG = 3;
    public static int WHAT_SHOW_LOGIN = 4;
    //for search
    public static in WHAT_SEARCH_COMPLETE = 5;

    //dialog ids.
    public static int DIALOG_PROGRESS = 1;

    //members.
    private Braindump mActivity;

    public ProgressDialogHandler(Braindump a){
        mActivity = a;
    }

    public void handleMessage(Message m){
        super.handleMessage(m);
        if(m.what == WHAT_SHOW_PROGRESS){
            Log.d(TAG, "showing dialog");
            mActivity.showDialog(DIALOG_PROGRESS);
        }
        else if(m.what == WHAT_HIDE_PROGRESS){
            Log.d(TAG, "attempting to dismiss");
            mActivity.dismissDialog(DIALOG_PROGRESS);
            Log.d(TAG, "dismissed");
        }
        else if(m.what == WHAT_SHOW_RESULT_DIALOG){
            HmResponse r = (HmResponse)m.obj;
            Toast.makeText(mActivity, r.getMessage(), Toast.LENGTH_LONG).show();
            if(r.getSuccess()){
                mActivity.clearTextField();
            }
        }
        else if(m.what == WHAT_SHOW_LOGIN){
            Log.d(TAG, "launching login activity");
            Intent i = new Intent("org.nerdcircus.android.hiveminder.LOGIN");
            mActivity.startActivity(i);
        }
        else if(m.what == WHAT_SEARCH_COMPLETE){
            Log.d(TAG, "search done. update ui.");
            mActivity.showSearchResults((HmResponse)m.obj);
        }
    }
}
