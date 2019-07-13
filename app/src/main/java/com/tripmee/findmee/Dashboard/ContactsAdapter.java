package com.tripmee.findmee.Dashboard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.Models.EmergencyContact;
import com.tripmee.findmee.R;
import com.tripmee.findmee.Utilities.ImageUtility;
import com.tripmee.findmee.Utilities.Utility;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsAdapter extends ArrayAdapter<EmergencyContact> {

    private UserGlobalHandler UserHandler;
    private EmergencyContact contact;
    private IDeleteButtonListener deleteButtonListener;
    private int mPosition;
    private ArrayList<EmergencyContact> mContacts;

    public ContactsAdapter(Context context, ArrayList<EmergencyContact> users) {
        super(context, 0, users);

        mContacts = users;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        contact = getItem(position);
        mPosition = position;
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_contact, parent, false);
        }
        UserHandler = UserGlobalHandler.get_instance();
        // Lookup view for data population
        TextView Name = (TextView) convertView.findViewById(R.id.name_listitem);
        TextView Mobile = (TextView) convertView.findViewById(R.id.mobile_listitem);
        CircleImageView mCircleImageView = (CircleImageView) convertView.findViewById(R.id.profile_image_listitem);
        // Populate the data into the template view using the data object
        Name.setText(contact.getUserName());
        Mobile.setText(contact.getMobile());
        String sImage = contact.get_FireBaseProfileImage();
        Button btnDelete = (Button) convertView.findViewById(R.id.btnDeleteContact);

        if (sImage !=null) {
            Bitmap imageBitmap = ImageUtility.StringToBitMap(sImage);
            mCircleImageView.setImageBitmap(imageBitmap);
        }

        btnDelete.setTag(Integer.valueOf(position));
        btnDelete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int position = (int)v.getTag();
                EmergencyContact c = getItem(position);
                String msg = getContext().getString(R.string.emergency_contact_delete);
                AlertMessage(msg,c);
            }
        });


        // Return the completed view to render on screen
        return convertView;
    }
    private void AlertMessage(String message, final EmergencyContact contact) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle("Confirm");
        builder.setMessage(message);

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                UserHandler.DeleteEmergencyContact(contact);
                dialog.dismiss();
                String msg = getContext().getString(R.string.emergency_contact_deleted);
                Utility.displayMessage(getContext(), msg);
                mContacts.remove(contact);
                notifyDataSetChanged();
                if (deleteButtonListener != null)
                {
                    deleteButtonListener.OnButtonClickListener(mPosition,contact);
                }
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
    public interface IDeleteButtonListener {
        void OnButtonClickListener(int position, EmergencyContact value);
    }
    public void SetDeleteButtonListener(IDeleteButtonListener listener)
    {
        this.deleteButtonListener = listener;
    }
}