package com.example.navischool;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

public class ChildListAdapter extends ArrayAdapter<String> {

    public ChildListAdapter(Context context, List<String> children) {
        super(context, android.R.layout.simple_list_item_1, children);
    }
}
