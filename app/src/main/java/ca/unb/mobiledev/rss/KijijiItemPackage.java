package ca.unb.mobiledev.rss;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class KijijiItemPackage extends BaseItemsPackage
{
    public KijijiItemPackage(String url) {
        super(url);
    }

    @Override
    public void fillFromParsedModel(List<ParsingDataModel> dataModel, String feedPublicationDate)
    {
        this.items = new ArrayList<>();

        for(ParsingDataModel modelItem: dataModel)
        {
            BaseItem item = new BaseItem();
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

    @Override
    public BaseItemsPackage.FeedUpdateInfo UpdateFromAnotherPackage(BaseItemsPackage otherPackage) {
        FeedUpdateInfo updateInfo = new FeedUpdateInfo();

        for(BaseItem newItem: this.items) {
            boolean hasMatch = false;
            for (BaseItem oldItem : otherPackage.items) {
                hasMatch = newItem.isSameItem(oldItem);
                if (hasMatch)
                {
                    //Copy over user has viewed flags
                    newItem.userHasViewed = oldItem.userHasViewed;

                    if(newItem.itemHasBeenUpdatedOnline(oldItem))
                    {
                        updateInfo.updatedItemsCount += 1;
                        newItem.isUpdated = true;
                        newItem.userHasViewed = false;
                    }
                    break;
                }
            }

            if (!hasMatch)
            {
                newItem.userHasViewed = false;
                updateInfo.newItemsCount += 1;
            }
        }

        return updateInfo;
    }

    @Override
    public BaseItemsPackage ParseRssFeed() throws IOException, XmlPullParserException {
        List<ParsingDataModel> items = new ArrayList<ParsingDataModel>();

        // Setup Parser.
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        InputStream stream = new ByteArrayInputStream(this.url.getBytes());
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

        this.fillFromParsedModel(items, feedPubDate);

        return this;
    }
}
