package com.tripmee.findmee.Models.CrimeReport;

import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;
import com.tripmee.findmee.Models.ImageObject;
import com.tripmee.findmee.Utilities.Utility;

import java.util.ArrayList;


public class CrimeReportList {

    @SerializedName("crimereport_list")
    private ArrayList<CrimeReport> _CrimeReports;

    private ArrayList<ImageObject> _CrimeImages;
    public ArrayList<CrimeReport> getCrimeReports() {
        return _CrimeReports;
    }


    public void setCrimeReportList(ArrayList<CrimeReport> reportList) {
        this._CrimeReports = reportList;
    }

    public void Add(CrimeReport report, Bitmap bitmap){

        _CrimeReports.add(report);

        if(!Utility.StringIsBlankOrEmpty(report.getImageUrl())) {
            ImageObject imageObject = new ImageObject(report.getID(), report.getImageUrl());

            if (bitmap != null) {
                imageObject.set_BitmapImage(bitmap);
            } else {
                imageObject.LoadImage();
            }
            _CrimeImages.add(imageObject);
        }
    }

    public void Update(CrimeReport report, Bitmap bitmap){

        for (CrimeReport crimeReport:_CrimeReports) {
            if(crimeReport.getID() == report.getID()){
                crimeReport.setComments(report.getComments());
                crimeReport.setCrimeType(report.getCrimeType());
                crimeReport.setDateTime(report.getDateTime());
                crimeReport.setDisplayName(report.getDisplayName());
                crimeReport.setImageUrl(report.getImageUrl());
                crimeReport.setUserID(report.getUserID());
                crimeReport.setLatitude(report.getLatitude());
                crimeReport.setLongitude(report.getLongitude());
            }
        }

        if (bitmap != null) {
            ImageObject img = new ImageObject(report.getID(), report.getImageUrl());
            img.set_BitmapImage(bitmap);
            _CrimeImages.add(img);
        }


    }

    public void LoadImages(){
        _CrimeImages = new ArrayList<ImageObject>();

        if(_CrimeReports == null){
            return;
        }

        for (CrimeReport r:_CrimeReports) {

            if(!Utility.StringIsBlankOrEmpty(r.getImageUrl())) {
                ImageObject imageObject = new ImageObject(r.getID(), r.getImageUrl());
                imageObject.LoadImage();
                _CrimeImages.add(imageObject);
            }
        }
    }
    public Bitmap GetImage(CrimeReport report){

        if(_CrimeImages == null){
            return null;
        }

        for (ImageObject image:_CrimeImages) {
            if (image.getID() == report.getID() && image.get_ImageUrl().equals(report.getImageUrl()) ){
                return image.get_BitmapImage();
            }
        }
        return null;
    }

    public void DeleteImage(String ImageUrl, Integer ReportID){
        int index = 0;
        for (ImageObject image:_CrimeImages) {
            if (image.getID() == ReportID && image.get_ImageUrl().equals(ImageUrl) ){
                _CrimeImages.remove(index);
            }
            index += 1;
        }
    }

    public void Delete(Integer ID){

        //Log.e("Before Deleted", "ID " + String.valueOf(ID));
        for(int i=0; i < _CrimeReports.size(); i++){
            CrimeReport r = _CrimeReports.get(i);
            if(ID == r.getID()){
                //Log.e("Deleted", "ID" + String.valueOf(sos.getId()));
                //Log.e("Deleted", "sos" + sos.getMessage());
                _CrimeReports.remove(i);
            }
        }

        for(int i=0; i<_CrimeImages.size(); i++){
            ImageObject image = _CrimeImages.get(i);
            if(ID == image.getID()){
                //Log.e("Deleted", "sos" + sosImage.get_ImageUrl());
                _CrimeImages.remove(i);
            }
        }
    }
}
