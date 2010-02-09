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

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View v = convertView;
        if(v == null){
            v = mInflater.inflate(R.layout.taskitem, null);
        }
        Task task = this.getItem(position);
        
        CheckBox cb = (CheckBox) v.findViewById(R.id.checkbox);
        cb.setText(task.getSummary());
        cb.setChecked(task.complete);

        return v;
    }

}

