package org.nerdcircus.android.hiveminder;

import org.nerdcircus.android.hiveminder.HmAuthException;
import org.nerdcircus.android.hiveminder.HmParseException;
import org.nerdcircus.android.hiveminder.parser.HmXmlParser;
import org.nerdcircus.android.hiveminder.model.*;

import android.content.SharedPreferences;
import android.content.Context;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;

import android.util.Log;
import java.util.Map;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


/* Class to represent the "Controller" backend of a web-service-backed activity.
 */
public abstract class WebActivityController {

    // used when this hivemind is passed around via onRetainLastNonConfigurationInstance()
    public abstract void setActivity(WebActivity a);
    public abstract WebActivity getActivity();

    /* Send a message to the Ui thread, via the activity's handler.
     */
    public boolean sendUiMessage(Message m){
        return this.getActivity().getHandler().sendMessage(m);
    }

}

