package org.nerdcircus.android.hiveminder.model;

import org.nerdcircus.android.hiveminder.model.Task;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/** HmResponse class represents a Response to an Hiveminder api call.
 */
public class HmResponse {
    private String TAG = "HmResponse";

    private Bundle mBundle;
    private List mTaskList;

    /** Task - constructor to make a new empty task.
     */
    public HmResponse(){
        mBundle = new Bundle();
        mTaskList = new ArrayList<Task>();
    }

    /* getters and setters for important fields */

    public void setSuccess(boolean status){
        this.mBundle.putBoolean("success", status);
    }
    public boolean getSuccess(){
        return this.mBundle.getBoolean("success");
    }
    public void setAction(String ac){
        this.mBundle.putString("action_class", ac);
    }
    public String getAction(){
        return this.mBundle.getString("action_class");
    }
    public void setMessage(String m){
        this.mBundle.putString("message", m);
    }
    public String getMessage(){
        return this.mBundle.getString("message");
    }

    //responses usually have an associated list of tasks...
    public List<Task> getTasks(){
        return this.mTaskList;
    }
    public boolean addTask(Task t){
        return this.mTaskList.add(t);
    }

}

