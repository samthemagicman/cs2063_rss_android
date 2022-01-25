package ca.unb.mobiledev.rss;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

interface OnTaskCompleted
{
    void onImageDownloadCompleted();
}

public class ImageParserUtilities
{
    public static class RetrieveImageTask extends AsyncTask<String, Void, Boolean>
    {
        private OnTaskCompleted listener;
        private KijijiParser.DataModel model;

        public RetrieveImageTask(KijijiParser.DataModel model, OnTaskCompleted listener)
        {
            this.listener = listener;
            this.model = model;
        }

        @Override
        protected Boolean doInBackground(String... strings) {

            Bitmap bitmap = null;
            InputStream in = null;
            URL url = null;
            try {
                url = new URL((String)model.entryModel.get(RssParserUtilities.GlobalTags.enclosure));

                HttpsURLConnection httpCon = (HttpsURLConnection)  url.openConnection();
                httpCon.setDoInput(true);
                httpCon.connect();
                in = httpCon.getInputStream();

                model.imageBitmap = BitmapFactory.decodeStream(in);
                in.close();

                return true;

            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            listener.onImageDownloadCompleted();
        }
    }
}
