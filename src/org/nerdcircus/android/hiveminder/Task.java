package org.nerdcircus.android.hiveminder;

import org.json.JSONObject;
import org.json.JSONException;

import android.os.Bundle;

import android.util.Log;

/** Task - a class to represent a Hiveminder Task
 * abstracts the mechanics of json api bits.
 */
public class Task {
    private String TAG = "HmTask";

    //jsonobject fields that are integers.
    private static String[] mIntFields = {
        "id",
        "priority",
        "complete",
        "group_id",
    };

    //jsonobject fields that are strings.
    private static String[] mStringFields = {
        "summary",
        "description",
        "created",
        "starts",
        "tags",
    };

    private Bundle mBundle;

    /** Task - constructor to make a Task out of a JSONObject.
     * JSONObject must be of the form delivered from the hm json interface
     */
    public Task(JSONObject j){
        //start with the few fields we care about.
        //TODO(muncus): fill in the rest of the fields

        Bundle b = new Bundle();
        for(int i=0; i < mIntFields.length; i++){
            try {
                b.putInt(mIntFields[i], j.getInt(mIntFields[i]));
            } catch (JSONException e){
                Log.w(TAG, "No value for field: "+mIntFields[i]);
            }
        }
        for(int i=0; i < mStringFields.length; i++){
            try {
                b.putString(mStringFields[i], j.getString(mStringFields[i]));
            } catch (JSONException e){
                Log.w(TAG, "No value for field: "+mStringFields[i]);
            }
        }
        this.mBundle = b;
    }

    /** getId - convenience function for fetching task id
     */
    public int getId(){
        return this.mBundle.getInt("id", -1);
    }

    //TODO(muncus): make a get() wrapper that auto-detects the type.

    /** getInt - easy wrapper to get items from the json object.
     */
    public int getInt(String key){
        return this.mBundle.getInt(key, -1);
    }

    /** getString - easy wrapper to get items from the json object.
     */
    public String getString(String key){
        String rval = this.mBundle.getString(key);
        if (rval == null){
            rval = "";
        }
        return rval;
    }
}

