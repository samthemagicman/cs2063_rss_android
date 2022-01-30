package ca.unb.mobiledev.rss;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

interface BaseItemsPackageInterface
{
    static class ParsingDataModel implements Serializable
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

    public void fillFromParsedModel(List<ParsingDataModel> dataModel, String feedPublicationDate);

    public BaseItemsPackage.FeedUpdateInfo UpdateFromAnotherPackage(BaseItemsPackage otherPackage);

    public BaseItemsPackage ParseRssFeed() throws IOException, XmlPullParserException;
}

public class BaseItemsPackage implements BaseItemsPackageInterface
{
    public ArrayList<BaseItem> items = new ArrayList<>();
    public Date feedPublicationDate = new Date();
    public String url = "";

    public BaseItemsPackage(String url)
    {
        this.url = url;
    }

    public static class FeedUpdateInfo
    {
        public int newItemsCount = 0;
        public int updatedItemsCount = 0;

        public boolean hasUpdates() {return this.newItemsCount > 0 || this.updatedItemsCount > 0;}
    }

    @Override
    public void fillFromParsedModel(List<ParsingDataModel> dataModel, String feedPublicationDate) {
        return;
    }

    @Override
    public BaseItemsPackage.FeedUpdateInfo UpdateFromAnotherPackage(BaseItemsPackage otherPackage) {
        return null;
    }

    @Override
    public BaseItemsPackage ParseRssFeed() throws IOException, XmlPullParserException {
        return null;
    }
}
