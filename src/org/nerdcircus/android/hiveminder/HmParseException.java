package org.nerdcircus.android.hiveminder;

import android.util.Log;

public class HmParseException extends Exception {

    /** simple exception class
     */
    private String TAG = "HmParseException";
    public HmParseException(String message){
        super(message);
    }
    public HmParseException(Exception e){
        super((Throwable) e);
    }
}

