package com.tripmee.findmee.EmergencyRequests;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tripmee.findmee.Models.EmergencyRequest;
import com.tripmee.findmee.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class NotificationAdapter extends ArrayAdapter<EmergencyRequest> {

    private View.OnClickListener OnClickListener;
    public NotificationAdapter(Context context, ArrayList<EmergencyRequest> Requests) {
        super(context, 0, Requests);


        Collections.sort(Requests, new Comparator<EmergencyRequest>() {
            public int compare(EmergencyRequest o1, EmergencyRequest o2) {
                return o2.getmDateTime().compareTo(o1.getmDateTime());
                //return (o1.getmDateTime() > o2.getmDateTime() ? -1 : 1);     //descending
                //  return (d1.getTime() > d2.getTime() ? 1 : -1);     //ascending
            }
        });

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        EmergencyRequest request = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listtextview, parent, false);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.listTextView);
        // Populate the data into the template view using the data object
        textView.setText(request.getEmergencyMessage());

        if (request.IsChecked()){
            textView.setTextColor(Color.GRAY);
        }else{
            textView.setTextColor(Color.RED);
            String s = request.getEmergencyMessage();
        }
        // Return the completed view to render on screen
        return convertView;
    }
}