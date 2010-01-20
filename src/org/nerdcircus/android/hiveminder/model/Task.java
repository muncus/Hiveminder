package org.nerdcircus.android.hiveminder.model;

import android.util.Log;

/** Task - a class to represent a Hiveminder Task
 * designed to be instantiated from an xml pull parser.
 */
public class Task {
    private String TAG = "Task";

    public long id; //task ID.
    public String summary; 

    public String getSummary(){
        return summary;
    }
}

