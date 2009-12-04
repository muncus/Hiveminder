package org.nerdcircus.android.hiveminder;


import org.nerdcircus.android.hiveminder.HmClient;
import org.nerdcircus.android.hiveminder.model.HmResponse;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.app.AlertDialog;
import android.app.ProgressDialog;

//little test icon.
import android.R.drawable;


import android.util.Log;

public class Login extends WebActivity
{
    private String TAG = "Login";

    private EditText mUsername;
    private EditText mPassword;
    private HmClient mHivemind;

    private Handler mHandler;
    private ProgressDialog mProgress;

    //keep a copy of our login response.
    private HmResponse mLoginResponse;
    private Activity mThis;


    private Runnable mShowResultDialog = new Runnable() {
        public void run() {
            if(mLoginResponse.getSuccess()){
                Log.d(TAG, "login worked!");
                mHivemind.saveSidCookie();
                AlertDialog.OnClickListener quitter =  new AlertDialog.OnClickListener(){
                    public void onClick(DialogInterface d, int button ){
                        mHandler.post(mFinish);
                    }
                };
                AlertDialog.Builder ab = new AlertDialog.Builder((Context)mThis);
                ab.setMessage(mLoginResponse.getMessage());
                ab.setNeutralButton("Ok", quitter);
                ab.create().show();
            }
            else{
                AlertDialog.Builder ab = new AlertDialog.Builder((Context)mThis);
                ab.setMessage(mLoginResponse.getMessage());
                ab.create().show();
            }
        }
    };

    public Handler setHandler(Handler h){
        mHandler = h;
        return mHandler;
    }

    public Handler getHandler(){
        return mHandler;
    }

    private Runnable mFinish = new Runnable() {
        public void run() {
            mThis.finish();
        }
    };

    private Runnable mShowProgress = new Runnable() {
        public void run() {
            mProgress = ProgressDialog.show(mThis, "Working", "logging in", true);
        }
    };

    private Runnable mHideProgress = new Runnable() {
        public void run() {
            mProgress.dismiss();
        }
    };

    /** Called with the activity is first created. */
    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);

        mThis = this;

        mHandler = new Handler();
        mHivemind = new HmClient(this);

        //use ssl, if prefs say so.
        mHivemind.setSsl(getPreferences(Context.MODE_PRIVATE).getBoolean("use_ssl", false));

        setContentView(R.layout.login);
        mUsername = (EditText)findViewById(R.id.username);
        mPassword = (EditText)findViewById(R.id.password);

        Button loginbtn = (Button)findViewById(R.id.login);
        loginbtn.setOnClickListener( new Button.OnClickListener(){
            public void onClick(View v){
                loginWithProgressDialog();
            }
        });

    }

    private void loginWithProgressDialog(){
        Thread t = new Thread(){
            public void run(){
                Log.d(TAG, "show progress dialog...");
                mHandler.post(mShowProgress);
                mLoginResponse = mHivemind.doLogin(
                                mUsername.getText().toString(),
                                mPassword.getText().toString()
                                );

                Log.d(TAG, "dismiss it.");
                mHandler.post(mHideProgress);
                mHandler.post(mShowResultDialog);
            }
        };
        t.start();
    }

}
