package com.tripmee.findmee.CrimeReport;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.Models.CrimeReport.UserComment;
import com.tripmee.findmee.Models.CrimeReport.UserCommentList;
import com.tripmee.findmee.Models.User;
import com.tripmee.findmee.Network.UnitOfWork;
import com.tripmee.findmee.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends ArrayAdapter<UserComment>{
    private UserComment _UserComment;
    private User _CurrentUser;
    private ArrayList<UserComment> _UserComments;
    private UserCommentList _UserCommentList;
    private TextView NameTextView;
    private CircleImageView mCircleImageView;
    private int mPosition;

    public CommentsAdapter(Context context, UserCommentList userCommentList){
        super(context, 0, userCommentList.get_UserComments());
        _CurrentUser = UserGlobalHandler.get_instance().CurrentUser;
        _UserCommentList = userCommentList;
        _UserComments = _UserCommentList.get_UserComments();
    }

    public int getCount() {

        if(_UserComments == null){
            return 0;
        }
        return _UserComments.size();
    }

    public UserComment getItem(int position) {
        return _UserComments.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        // Get the data item for this position
        mPosition = position;
        _UserComment = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_comment, parent, false);
        }

        convertView.setTag(Integer.valueOf(position));
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int position = (int)v.getTag();
                UserComment userComment = getItem(position);

                if(userComment != null){
                    if(userComment.getUserID().equals(_CurrentUser.getUserID())){
                        // if a comment is posted by current user, the person is allowed to delete the comment
                        AlertDeleteCommentMessage(getContext().getResources().getString(R.string.item_delete), userComment.getCommentID());
                    }
                }
            }
        });
        //convertView.setFocusable(false);
        NameTextView = (TextView) convertView.findViewById(R.id.textViewUsername);
        TextView textDateTime = (TextView) convertView.findViewById(R.id.textViewDateTime);
        TextView Message = (TextView) convertView.findViewById(R.id.textViewComment);
        //mCircleImageView = (CircleImageView) convertView.findViewById(R.id.imageViewProfile);
        // Populate the data into the template view using the data object

        NameTextView.setText(_UserComment.getUserName());
        Message.setText(_UserComment.getText());
        textDateTime.setText(_UserComment.getDateTime());

        /*_UserComment.dataCompleteCallBack = new ActionCompleteCallBack() {
            @Override
            public void CallBack() {
                SetupUserDetails();
            }
        };*/


        /*btnAccept.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String msg = getContext().getString(R.string.handshake_request_accept);

                AlertMessage(msg,mUser);

            }
        });

        btnReject.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserHandler.DeleteHandShakeRequest(mUser);
                mRequests.remove(mUser);
                notifyDataSetChanged();

                if (rejectButtonListener != null)
                {
                    rejectButtonListener.OnButtonClickListener(mPosition,mUser);
                }
            }
        });*/
        // Return the completed view to render on screen
        return convertView;
    }
    static class ViewHolder {
        TextView text;
    }

    private void AlertDeleteCommentMessage(String message, final String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle("Confirm");
        builder.setMessage(message);

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                UnitOfWork UOW = UnitOfWork.get_instance(UserGlobalHandler.get_instance().CurrentUser);
                UOW.DeleteComment(id);
                //_UserComments.remove(position);
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

    /*private void SetupUserDetails(){

        if(_UserComment._User != null) {
            NameTextView.setText(_UserComment._User.getUserName());
            mCircleImageView.setImageBitmap(_UserComment._User .GetProfileImage());
        }

    }*/


}
