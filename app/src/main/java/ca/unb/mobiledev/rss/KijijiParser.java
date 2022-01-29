package ca.unb.mobiledev.rss;

import android.graphics.Bitmap;
import android.location.Location;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Date;

public class KijijiParser
{
    public static class KijijiItem implements Serializable, Comparable<KijijiItem>
    {
        String title = "";
        String link = "";
        String description = "";
        String bitmapLink = "";
        String publicationDate = "";
        Date dateTimestamp = new Date();
        double lat = 0.0;
        double lon = 0.0;
        String price = "";

        Bitmap bitmapImage = null;

        @Override
        public int compareTo(KijijiItem o) {
            boolean titleMatch = this.title.equals(o.title);
            boolean linkMatch = this.link.equals(o.link);
            boolean dateMatch = (this.dateTimestamp.getTime() - o.dateTimestamp.getTime() ) == 0.0;

            if(titleMatch && linkMatch && dateMatch) return 0;
            else return 1;
        }
    }

    public static class KijijiRssPackage implements Serializable
    {
        ArrayList<KijijiItem> items = new ArrayList<>();
        Date feedPublicationDate = new Date();

        public KijijiRssPackage(List<ParsingDataModel> dataModel, String feedPublicationDate)
        {
            items = new ArrayList<>();

            for(ParsingDataModel modelItem: dataModel)
            {
                KijijiItem item = new KijijiItem();
                item.title = modelItem.entryModel.get(RssParserUtilities.GlobalTags.title).toString();
                item.link = modelItem.entryModel.get(RssParserUtilities.GlobalTags.link).toString();
                item.description = modelItem.entryModel.get(RssParserUtilities.GlobalTags.description).toString();
                item.bitmapLink = modelItem.entryModel.get(RssParserUtilities.GlobalTags.enclosure).toString();
                item.publicationDate = modelItem.entryModel.get(RssParserUtilities.GlobalTags.pubDate).toString();
                item.price = modelItem.entryModel.get(RssParserUtilities.GlobalTags.price).toString();
                item.lat = Double.parseDouble(modelItem.entryModel.get(RssParserUtilities.GlobalTags.lat).toString());
                item.lon = Double.parseDouble(modelItem.entryModel.get(RssParserUtilities.GlobalTags.lon).toString());

                String pubDate = modelItem.entryModel.get(RssParserUtilities.GlobalTags.dcDate).toString();
                try{
                    item.dateTimestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(pubDate);
                }
                catch(ParseException e)
                {
                    e.printStackTrace();
                }

                items.add(item);
            }

            String feedPubDate = feedPublicationDate;
            try {
                this.feedPublicationDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(feedPubDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private static class ParsingDataModel implements Serializable
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
    }

    public static KijijiRssPackage ParseRssFeed(String rssFeedUrl) throws IOException, XmlPullParserException
    {
        List<ParsingDataModel> items = new ArrayList<ParsingDataModel>();

        // Setup Parser.
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        InputStream stream = new ByteArrayInputStream(rssFeedUrl.getBytes());
        parser.setInput(stream, null);
        parser.nextTag();

        // Read the feed.
        String namespace = null;
        String feedPubDate = "";

        parser.require(XmlPullParser.START_TAG, namespace, "rss"); //STart tag for kijiji <rss xmlns:atom=...
        while(parser.next() != XmlPullParser.END_DOCUMENT)
        {
            if(parser.getEventType() != XmlPullParser.START_TAG) { continue; }

            String name = parser.getName();

            if(name.equals("channel")) // get the rss feed publication data for notification.
            {
                LinkedHashMap<RssParserUtilities.GlobalTags, Object> pubDateItem;
                {
                    pubDateItem = new LinkedHashMap<>();
                    pubDateItem.put(RssParserUtilities.GlobalTags.dcDate, "N/A");
                }

                RssParserUtilities.parseEntry(parser, pubDateItem);
                feedPubDate = pubDateItem.get(RssParserUtilities.GlobalTags.dcDate).toString();
            }

            ParsingDataModel model = new ParsingDataModel();
            if(name.equals("item")) // Parse each kijiji item
            {
                RssParserUtilities.parseEntry(parser, model.entryModel);
                items.add(model);
            }
        }

        KijijiRssPackage kijijiPackage = new KijijiRssPackage(items, feedPubDate);

        return kijijiPackage;
    }


}
