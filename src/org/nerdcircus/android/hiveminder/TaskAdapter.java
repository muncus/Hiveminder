package org.nerdcircus.android.hiveminder;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import org.nerdcircus.android.hiveminder.model.Task;

public class TaskAdapter extends ArrayAdapter<Task>
{
    private String TAG = "TaskAdapter";
    private LayoutInflater mInflater;

    public TaskAdapter(Context context, int layout){
        super(context, layout, R.id.checkbox);
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /*
    public void bindView(View view, Context context, Task task){
        CheckBox desc = (CheckBox) view.findViewById(R.id.checkbox);
        desc.setText(task.getSummary());
        desc.setChecked(task.complete);
        Log.d(TAG, "in bind view");
        if(task.complete == true){
            Log.d(TAG, "should be checked!");
        }

    }
    */

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        Log.d(TAG, "in get view");
        View v = convertView;
        if(v == null){
            Log.d(TAG, "null convert view");
            v = mInflater.inflate(R.layout.taskitem, null);
            Log.d(TAG, "Inflate WORKED!");
            Log.d(TAG, v.toString());
        }
        Task task = this.getItem(position);
        Log.d(TAG, task.getSummary());
        Log.d(TAG, v.toString());

        
        Log.d(TAG, "attempting fvbi()");
        CheckBox cb = (CheckBox) v.findViewById(R.id.checkbox);
        //CheckBox cb = (CheckBox) ((ViewGroup)v).getChildAt(0); //XXX: we only have one child, for now.
        Log.d(TAG, "done with fvbi()");
        cb.setText(task.getSummary());
        cb.setChecked(task.complete);
        if(task.complete == true){
            Log.d(TAG, "should be checked!");
        }

        return v;
    }

}

