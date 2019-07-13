package com.tripmee.findmee.Models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

    private Listener listener;
    public DownloadImageTask(final Listener listener) {
        this.listener = listener;
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        // Logic to download an image from an URL
        final String url = urls[0];
        Bitmap bitmap = null;

        try {
            final InputStream inputStream = new URL(url).openStream();
            bitmap = BitmapFactory.decodeStream(inputStream);

        } catch (final MalformedURLException malformedUrlException) {
            // Handle error
            Log.e("DownloadImageTask", malformedUrlException.getMessage());
        } catch (final IOException ioException) {
            Log.e("DownloadImageTask", ioException.getMessage());
        }
        return bitmap;
    }
    /*
    @Override
    protected Bitmap doInBackground(String... urls) {
        int count;


        try {
            URL url = new URL(urls[0]);
            HttpURLConnection connection2 = (HttpURLConnection) url.openConnection();
            connection2.setRequestMethod("GET");
            connection2.setDoOutput(true);
            connection2.connect();
            int lenghtOfFile = connection2.getContentLength();

            //File apkdir = new File(PATH);
            //apkdir.mkdirs();

            //File newInstall = new File(PATH, name+".tmp");

            InputStream input = new BufferedInputStream(url.openStream());
            //OutputStream output = new FileOutputStream(newInstall);

            //byte data[] = new byte[1024];

            //long total = 0;

           //while ((count = input.read(data)) != -1 && running==true) {
                //total += count;
                //publishProgress((int) (total * 100 / lenghtOfFile));
                //output.write(data, 0, count);
            //}
            //output.flush();
            //output.close();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            input.close();
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }*/



    @Override
    protected void onPostExecute(Bitmap downloadedBitmap) {
        if (null != downloadedBitmap) {
            listener.onImageDownloaded(downloadedBitmap);
        } else {
            listener.onImageDownloadError();
        }
    }

    public static interface Listener {
        void onImageDownloaded(final Bitmap bitmap);
        void onImageDownloadError();
    }
}