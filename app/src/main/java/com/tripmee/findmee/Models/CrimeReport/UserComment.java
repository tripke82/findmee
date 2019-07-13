package com.tripmee.findmee.Models.CrimeReport;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.tripmee.findmee.CallBack.ActionCompleteCallBack;
import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.Models.User;

public class UserComment extends Comment {

    public User _User;
    public ActionCompleteCallBack dataCompleteCallBack;

    public UserComment(User user, Comment comment){
        super(comment.getReportID(),comment.getUserID(),
                comment.getCommentID(), comment.getUserName(), comment.getText(),comment.getDateTime());

        if(comment.getId() !=null){
            super.setId(comment.getId());
        }
        _User = user;
    }

    public UserComment(int reportID, String userID, String commentID, String name, String text, String dateTime){
        super(reportID,userID,commentID, name, text,dateTime);
        //Init_User();
    }

    private void Init_User(){
        DatabaseReference ref = UserGlobalHandler.get_instance().GetDataBaseRefByUserID(super.getUserID());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                _User =  UserGlobalHandler.get_instance().GetUserByDataSnapShot(dataSnapshot);

                if(dataCompleteCallBack != null){
                    dataCompleteCallBack.CallBack();;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
