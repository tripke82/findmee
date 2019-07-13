package com.tripmee.findmee.Models.CrimeReport;

import com.tripmee.findmee.Models.User;

import java.util.ArrayList;

public class UserCommentList {

    private ArrayList<com.tripmee.findmee.Models.CrimeReport.UserComment> _UserComments = new ArrayList<com.tripmee.findmee.Models.CrimeReport.UserComment>();

    public void set_UserComments(ArrayList<com.tripmee.findmee.Models.CrimeReport.UserComment> _UserComments) {
        this._UserComments = _UserComments;
    }

    public ArrayList<com.tripmee.findmee.Models.CrimeReport.UserComment> get_UserComments() {
        return _UserComments;
    }

    public void Add(User CurrentUser, Comment comment){

        //_Comments.add(comment1);
        com.tripmee.findmee.Models.CrimeReport.UserComment userComment = new com.tripmee.findmee.Models.CrimeReport.UserComment(CurrentUser, comment);
        userComment.setId(comment.getId());
        _UserComments.add(userComment);
    }

    public void AddComments(ArrayList<Comment> Comments){
        _UserComments.clear();
        if(Comments != null) {
            for (Comment comment : Comments) {

                com.tripmee.findmee.Models.CrimeReport.UserComment userComment = new com.tripmee.findmee.Models.CrimeReport.UserComment(comment.getReportID(), comment.getUserID(),
                        comment.getCommentID(), comment.getUserName(), comment.getText(), comment.getDateTime());
                userComment.setId(comment.getId());
                _UserComments.add(userComment);
            }
        }
    }

    public void Delete(String commentID){

        for(int i=0; i<_UserComments.size(); i++){
            com.tripmee.findmee.Models.CrimeReport.UserComment comment = _UserComments.get(i);
            if(comment.getCommentID().equals(commentID)){
                System.out.println("Removing " + comment.getText());
                _UserComments.remove(i); // will throw CME
            }
        }
    }
}
