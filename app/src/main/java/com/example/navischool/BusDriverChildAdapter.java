package com.example.navischool;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.List;

public class BusDriverChildAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final List<String> groupedChildren;

    public BusDriverChildAdapter(Context context, List<String> groupedChildren) {
        super(context, 0, groupedChildren);
        this.context = context;
        this.groupedChildren = groupedChildren;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView textView = convertView.findViewById(android.R.id.text1);

        String item = groupedChildren.get(position);

        if (item.startsWith("Address:")) {
            textView.setText(item);
            textView.setTextColor(Color.parseColor("#FF5722"));
            textView.setPadding(0, 20, 0, 10);
        } else {
            textView.setText(item);
            textView.setPadding(0, 5, 0, 5);
        }

        if (!item.startsWith("Address:")) {
            convertView.setOnClickListener(v -> {
                Toast.makeText(context, item + " is almost home.", Toast.LENGTH_SHORT).show();
                ((BusDriverActivity) context).removeChildFromSession(item);
            });
        }

        return convertView;
    }
}