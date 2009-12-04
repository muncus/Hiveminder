package org.nerdcircus.android.hiveminder;

import android.util.Log;

public class HmAuthException extends Exception {

    /** simple exception class
     */
    private String TAG = "HmAuthException";
    public HmAuthException(String message){
        super(message);
    }
    public HmAuthException(){
        super();
    }
}

