package com.tripmee.findmee.Models;

import android.graphics.Bitmap;
import android.util.Log;

import com.tripmee.findmee.GlobalConstants.GlobalConstants;
import com.tripmee.findmee.Utilities.ImageUtility;

public class ImageObject {

    private Integer ID;
    private String _ImageUrl = "";
    private Bitmap _BitmapImage = null;

    public ImageObject(Integer id, String sUrl) {
        _ImageUrl = sUrl;
        ID = id;
    }

    public void set_BitmapImage(Bitmap _BitmapImage) {
        this._BitmapImage = _BitmapImage;
    }

    public Bitmap get_BitmapImage() {
        return _BitmapImage;
    }

    public Integer getID() {
        return ID;
    }

    public String get_ImageUrl() {
        return _ImageUrl;
    }

    public void LoadImage(){

        if (_ImageUrl.isEmpty()){
            return;
        }
        new DownloadImageTask(new DownloadImageTask.Listener() {
            @Override
            public void onImageDownloaded(Bitmap bitmap) {

                int i = bitmap.getByteCount();
                _BitmapImage = ImageUtility.resizeBitmap(bitmap, GlobalConstants.LISTVIEW_IMAGE_SIZE);
            }

            @Override
            public void onImageDownloadError() {
                Log.e("onImageDownloadError", "Failed to download image from "
                        + _ImageUrl);
            }
        }).execute(_ImageUrl);

    }
}
