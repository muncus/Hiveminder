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
import org.nerdcircus.android.hiveminder.ProgressDialogHandler;

import android.util.Log;

/** WebActivity - An Activity class for use with Web Service-backed activities
 * It acts as a "View" in the Model-View-Controller since (not to be confused
 * with android's View classes.
 */
public abstract class WebActivity extends Activity
{
    public abstract Handler getHandler();
    public abstract Handler setHandler(Handler h);

    /** associate the supplied "controller" with this "view"
     */
    public void setController(HmClient wac){
        wac.setActivity(this);
    }

}
