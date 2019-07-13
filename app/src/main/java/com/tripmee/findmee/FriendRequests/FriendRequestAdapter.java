package com.tripmee.findmee.FriendRequests;

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

import com.tripmee.findmee.GlobalConstants.FriendRequestConstants;
import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.Models.User;
import com.tripmee.findmee.R;
import com.tripmee.findmee.Utilities.ImageUtility;
import com.tripmee.findmee.Utilities.Utility;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestAdapter extends ArrayAdapter<User> {
    private User mUser;
    private UserGlobalHandler UserHandler;
    private ArrayList<User> mRequests;
    private int mPosition;
    private IRejectButtonListener rejectButtonListener;

    public FriendRequestAdapter(Context context, ArrayList<User> FriendRequestsReceived) {
        super(context, 0, FriendRequestsReceived);

        mRequests = FriendRequestsReceived;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        mPosition = position;
        mUser = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_friendrequest, parent, false);
        }
        UserHandler = UserGlobalHandler.get_instance();
        // Lookup view for data population
        TextView Name = (TextView) convertView.findViewById(R.id.request_name_listitem);
        TextView Mobile = (TextView) convertView.findViewById(R.id.request_mobile_listitem);
        CircleImageView mCircleImageView = (CircleImageView) convertView.findViewById(R.id.profile_image_friendrequestlistitem);
        Button btnAccept = (Button) convertView.findViewById(R.id.btnAccept);
        Button btnReject = (Button) convertView.findViewById(R.id.btnReject);

        // Populate the data into the template view using the data object
        Name.setText(mUser.getUserName());
        Mobile.setText(mUser.getMobile());
        String sImage = mUser.get_FireBaseProfileImage();

        mCircleImageView.setTag(Integer.valueOf(position));
        //mCircleImageView.setImageBitmap(mUser.GetProfileImage());
        if (sImage !=null) {
            String name = mUser.getUserName();
            int p1 = position;
            int p2 = (int)mCircleImageView.getTag();
            Bitmap imageBitmap = ImageUtility.StringToBitMap(sImage);
            mCircleImageView.setImageBitmap(imageBitmap);
        }else{
            mCircleImageView.setImageResource(R.drawable.default_profile);
        }

        btnAccept.setTag(Integer.valueOf(position));
        btnAccept.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                int position = (int)v.getTag();
                User user = getItem(position);

                if(UserHandler.GetEmergencyContactsCount() >= FriendRequestConstants.MAX_CONNECTIONS){
                    Utility.displayMessage(getContext(), getContext().getString(R.string.Number_Of_friends_exceeded));
                }else {
                    AlertMessage(getContext().getString(R.string.friend_request_accept), user);
                }

            }
        });

        btnReject.setTag(Integer.valueOf(position));
        btnReject.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int position = (int)v.getTag();
                User user = getItem(position);

                UserHandler.DeleteFriendRequest(user);
                mRequests.remove(user);
                notifyDataSetChanged();

                if (rejectButtonListener != null)
                {
                    rejectButtonListener.OnButtonClickListener(mPosition,user);
                }
            }
        });
        // Return the completed view to render on screen
        return convertView;
    }

    public interface IRejectButtonListener {
        void OnButtonClickListener(int position, User value);
    }
    public void SetRejectButtonListener(IRejectButtonListener listener)
    {
        this.rejectButtonListener = listener;
    }
    private void AlertMessage(String message, final User newContact) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle("Confirm");
        builder.setMessage(message);

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                UserHandler.AcceptFriendRequest(newContact);
                dialog.dismiss();
                String msg = getContext().getString(R.string.friend_request_accepted);
                Utility.displayMessage(getContext(), msg);
                mRequests.remove(mUser);
                notifyDataSetChanged();
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
}