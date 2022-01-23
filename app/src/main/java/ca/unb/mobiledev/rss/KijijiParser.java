package ca.unb.mobiledev.rss;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

interface OnTaskCompleted
{
    void onTaskCompleted();
}

public class KijijiParser
{
    public static class DataModel implements Serializable
    {
        LinkedHashMap<RssParserUtilities.GlobalTags, Object> entryModel;
        {
            entryModel = new LinkedHashMap<>();
            entryModel.put(RssParserUtilities.GlobalTags.title, "N/A");
            entryModel.put(RssParserUtilities.GlobalTags.link, "N/A");
            entryModel.put(RssParserUtilities.GlobalTags.description, "N/A");
            entryModel.put(RssParserUtilities.GlobalTags.enclosure, "N/A");
            entryModel.put(RssParserUtilities.GlobalTags.pubDate, "N/A");
            entryModel.put(RssParserUtilities.GlobalTags.dcDate, "N/A");
            entryModel.put(RssParserUtilities.GlobalTags.lat, "0.0");
            entryModel.put(RssParserUtilities.GlobalTags.lon, "0.0");
            entryModel.put(RssParserUtilities.GlobalTags.price, "0.0");
        }

        Bitmap imageBitmap = null;
    }

    public static List<DataModel> parseRssFeed(String rssFeed) throws IOException, XmlPullParserException
    {
        List<DataModel> items = new ArrayList<DataModel>();

        // Setup Parser.
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        InputStream stream = new ByteArrayInputStream(rssFeed.getBytes());
        parser.setInput(stream, null);
        parser.nextTag();

        // Read the feed.
        String namespace = null;
        parser.require(XmlPullParser.START_TAG, namespace, "rss"); //STart tag for kijiji <rss xmlns:atom=...
        while(parser.next() != XmlPullParser.END_DOCUMENT)
        {
            if(parser.getEventType() != XmlPullParser.START_TAG) { continue; }

            String name = parser.getName();
            if(name.equals("item"))
            {
                DataModel model = new DataModel();
                RssParserUtilities.parseEntry(parser, model.entryModel);
                items.add(model);
            }
        }

        return items;
    }

    public static class RetrieveImageTask extends AsyncTask<String, Void, Boolean>
    {
        private OnTaskCompleted listener;
        private DataModel model;

        public RetrieveImageTask(DataModel model, OnTaskCompleted listener)
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
            listener.onTaskCompleted();
        }
    }
}
