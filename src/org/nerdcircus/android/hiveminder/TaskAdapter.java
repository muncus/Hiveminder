package org.nerdcircus.android.hiveminder;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import android.util.Log;

import org.nerdcircus.android.hiveminder.model.Task;

public class TaskAdapter extends ArrayAdapter
{
    private String TAG = "TaskAdapter";

    public TaskAdapter(Context context, int layout){
        super(context, layout, R.id.checkbox);
    }

    public void bindView(View view, Context context, Task task){
        TextView desc = (TextView) view.findViewById(R.id.checkbox);
        desc.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        desc.setText(task.getSummary());
    }

}

