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


}
