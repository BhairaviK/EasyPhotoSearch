package com.microsoft.projectoxford.visionsample.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalysisResult;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;
import com.microsoft.projectoxford.visionsample.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class NewMediaReceiver extends BroadcastReceiver {
    private VisionServiceClient client;
    private SQLHelper sqlHelper;

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isNewPic = android.hardware.Camera.ACTION_NEW_PICTURE.equals(intent.getAction());
        if (!isNewPic) return;

        if (client == null){
            client = new VisionServiceRestClient(context.getString(R.string.subscription_key), "https://westcentralus.api.cognitive.microsoft.com/vision/v1.0");
        }
        if(sqlHelper == null) {
            sqlHelper = new SQLHelper(context);
        }

        ConnectivityManager cs = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cs.getActiveNetworkInfo();
        if (info == null) {
            return;
        }

        if (info.isConnected()) {
            Uri fileUri;
            Uri contentUri = intent.getData();
            if (contentUri != null && "content".equals(contentUri.getScheme())) {
                Cursor cursor = context.getContentResolver().query(contentUri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
                cursor.moveToFirst();
                fileUri = Uri.parse("file://" + cursor.getString(0));
                cursor.close();
            } else {
                fileUri = contentUri;
            }
            Bitmap bitmap = ImageHelper.loadSizeLimitedBitmapFromUri(fileUri, context.getContentResolver());
            new doAnalyzeRequest(fileUri,bitmap).execute();
        } else {
//            Log.d("davsync", "Queueing " + uri + "for later (not on WIFI)");
            // otherwise, queue the image for later
            //DavSyncOpenHelper helper = new DavSyncOpenHelper(context);
            // helper.queueUri(uri);
        }
    }

    private class doAnalyzeRequest extends AsyncTask<String, String, String> {
        // Store error message
        private Exception e = null;
        private Uri uri;
        private Bitmap bitmap;

        public doAnalyzeRequest(Uri uri, Bitmap bitmap) {
            this.uri = uri;
            this.bitmap = bitmap;
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                return process();
            } catch (Exception e) {
                this.e = e;    // Store error
            }

            return null;
        }

        private String process() throws VisionServiceException, IOException {
            String result = "Error while processing";
            Gson gson = new Gson();
            String[] features = {"ImageType", "Color", "Faces", "Adult", "Categories"};
            String[] details = {};

            // Put the image into an input stream for detection.
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            if(bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

                AnalysisResult v = client.analyzeImage(inputStream, features, details);

                result = gson.toJson(v);
                sqlHelper.insert(uri, result);
            }
            Log.d("result", result);

            return result;
        }
    }
}
