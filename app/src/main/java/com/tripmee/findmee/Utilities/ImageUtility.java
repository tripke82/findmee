package com.tripmee.findmee.Utilities;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.content.res.AppCompatResources;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.tripmee.findmee.GlobalConstants.GlobalConstants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

public class ImageUtility {

    public static String UploadImage(InputStream image, int imageLength, String imageName) throws Exception {
        CloudBlobContainer container = getContainer();

        container.createIfNotExists();

        CloudBlockBlob imageBlob = container.getBlockBlobReference(imageName);
        imageBlob.upload(image, imageLength);

        return imageBlob.getStorageUri().getPrimaryUri().toString();
    }

    public static CloudBlobContainer getContainer() throws Exception {
        // Retrieve storage account from connection-string.

        CloudStorageAccount storageAccount = CloudStorageAccount
                .parse(GlobalConstants.STORAGE_CONNECTION_STRING);

        // Create the blob client.
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

        // Get a reference to a container.
        // The container name must be lower case
        CloudBlobContainer container = blobClient.getContainerReference("images");

        return container;
    }

    public static Bitmap resizeStringToBitmap(String photoPath, int targetW, int targetH) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        }

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true; //Deprecated API 21

        return BitmapFactory.decodeFile(photoPath, bmOptions);
    }
    public static Bitmap resizeBitmap(Bitmap bitmap, int maxImageSize) {
        int photoW = bitmap.getWidth();
        int photoH = bitmap.getHeight();

        float ratio = Math.min(
                (float) maxImageSize / bitmap.getWidth(),
                (float) maxImageSize / bitmap.getHeight());

        if (ratio < 1) { //only scale down, don't scale up
            photoW = Math.round((float) ratio * bitmap.getWidth());
            photoH = Math.round((float) ratio * bitmap.getHeight());
        }

        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, photoW, photoH,true);
        return newBitmap;

    }
    public static Bitmap StringToBitMap(String encodedString){
        try{
            byte [] encodeByte= Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }catch(Exception e){
            e.getMessage();
            return null;
        }
    }
    public static String GetResizedBitmapToString(Bitmap bitmap){
        if(bitmap.getWidth()> GlobalConstants.IMAGE_MAX_BITMAP_DIMENSION
                || bitmap.getHeight() > GlobalConstants.IMAGE_MAX_BITMAP_DIMENSION)
        {
            bitmap = resizeBitmap(bitmap,GlobalConstants.IMAGE_MAX_BITMAP_DIMENSION);
        }

        Log.e("Original   dimensions", bitmap.getWidth()+" "+bitmap.getHeight());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

        Log.e("Compressed dimensions", bitmap.getWidth()+" "+bitmap.getHeight());
        String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

        return imageEncoded;
    }


    public static String BitMapToString(Bitmap bitmap){

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

            return imageEncoded;
        }catch (Exception e){
            e.getMessage();
            return null;
        }
    }

    public static Bitmap loadBitmap(String url) {
        Bitmap bitmap = null;
        try {

            url = "https://www.lifespan.org/sites/default/files/lifespan-files/images/centers/findmee-services-rih/emergency_services_rih.jpg";

            //InputStream inputStream = (InputStream)new URL(url).getContent();
            InputStream inputStream =  new java.net.URL(url).openStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static Bitmap GetBitmapByUrl(String sUrl){
        Bitmap imageBitmap = null;
        if (!com.tripmee.findmee.Utilities.Utility.StringIsBlankOrEmpty(sUrl)) {
            imageBitmap = StringToBitMap(sUrl);
            return imageBitmap;
        }else{
            return null;
        }
    }

    public static Bitmap getFacebookPhoto(Context inContext, Uri uri) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(inContext.getContentResolver(), uri);
        return bitmap;
    }

    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public static String getRealPathFromURI(Uri uri, Context context) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    public static InputStream PathToInputStream(String path) throws IOException {
        try {
            InputStream inputStream = new FileInputStream(path);
            return inputStream;

        }catch(IOException e){
            e.getMessage();
            return null;
        }
    }

    public static InputStream BitmapToInputStream(Bitmap bm) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        return is;

    }

    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {

        //Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        Drawable vectorDrawable = AppCompatResources.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public static InputStream saveBitmapToFile(File file){
        try {

            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 6;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE=75;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);
            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();
            //Create a new image file and then return it.
            //file.createNewFile();
            //FileOutputStream outputStream = new FileOutputStream(file);
            //selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100 , outputStream);
            return inputStream;
        } catch (Exception e) {
            return null;
        }
    }
}
