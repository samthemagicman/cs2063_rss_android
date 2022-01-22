package ca.unb.mobiledev.rss;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RssParserUtilities
{
    public enum GlobalTags {
        ERROR("error-no-conversion"),
        title("title"),
        link("link"),
        description("description"),
        enclosure("enclosure"),
        pubDate("pubDate"),
        guid("guid"),
        dcDate("dc:date"),
        lat("geo:lat"),
        lon("geo:long"),
        price("g-core:price");
        // INFO: - Add any new tags to this global list.
        // INFO: - Add new switch:case in parseEntry switch statement.

        private final String text;

        GlobalTags(final String text) {
            this.text = text;
        }

        @Override
        public final String toString() {
            return text;
        }

        public static final GlobalTags toEnum(String string)
        {
            for(GlobalTags t : GlobalTags.values())
            {
                if(t.text.equalsIgnoreCase(string))
                    return t;
            }

            return GlobalTags.ERROR;
        }
    }

    public static void parseEntry(XmlPullParser parser, LinkedHashMap<GlobalTags, Object> parserMap) throws IOException, XmlPullParserException
    {
        while(parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) { continue; }

            GlobalTags xmlTagEnum = GlobalTags.toEnum(parser.getName());

            boolean didFindMatch = false;
            for (Map.Entry<GlobalTags, Object> item : parserMap.entrySet())
            {
                if(didFindMatch) break;

                GlobalTags itemTag = item.getKey();
                didFindMatch = itemTag.equals(xmlTagEnum);
                if(!didFindMatch) continue;

                switch (xmlTagEnum)
                {
                    case title:
                    case link:
                    case description:
                    case pubDate:
                    case dcDate:
                    case guid:
                        parseGenericAsString(parser, itemTag.toString(), item);
                        break;

                    case enclosure:
                    {
                        String imageUrl = parseEnclosureTag(parser);
                        item.setValue(imageUrl);
                        break;
                    }

                    case lat:
                    case lon:
                    case price:
                        parseGenericAsDouble(parser, itemTag.toString(), item);
                        break;

                    default:
                        skipTag(parser);

                }
            }

            if(!didFindMatch)
                skipTag(parser);
        }
    }

    private static void skipTag(XmlPullParser parser) throws IOException, XmlPullParserException
    {
        int skipping = 1;
        while(skipping != 0)
        {
            int result = parser.next();
            if(result == XmlPullParser.END_TAG) skipping--;
            else if(result == XmlPullParser.START_TAG) skipping++;
        }
    }

    protected static Double parseGenericAsDouble(XmlPullParser parser, String tag) throws IOException, XmlPullParserException, NumberFormatException
    {
        String str = parseGenericAsString(parser, tag);
        Double val = Double.parseDouble(str);
        return val;
    }

    protected static void parseGenericAsDouble(XmlPullParser parser, String tag, LinkedHashMap.Entry<GlobalTags, Object> item) throws IOException, XmlPullParserException
    {
        Double val = parseGenericAsDouble(parser, tag);
        item.setValue(val);
    }

    protected static String parseGenericAsString(XmlPullParser parser, String tag) throws IOException, XmlPullParserException
    {
        parser.require(XmlPullParser.START_TAG, null, tag);
        String item = parseText(parser);
        parser.require(XmlPullParser.END_TAG, null, tag);
        return item;
    }

    protected static void parseGenericAsString(XmlPullParser parser, String tag, LinkedHashMap.Entry<GlobalTags, Object> item) throws IOException, XmlPullParserException
    {
        String str = parseGenericAsString(parser, tag);
        item.setValue(str);
    }

    protected static String parseText(XmlPullParser parser) throws IOException, XmlPullParserException
    {
        String text = "";
        if(parser.next() == XmlPullParser.TEXT)
        {
            text = parser.getText();
            //String text2 = parser.getText();
            parser.nextTag();
        }
        return text;
    }

    private static String parseEnclosureTag(XmlPullParser parser) throws IOException, XmlPullParserException
    {
        String imageUrl = "";
        String length = "";
        String imageType = "";

        parser.require(XmlPullParser.START_TAG, null, "enclosure");
        String tag = parser.getName();
        if(tag.equals("enclosure"))
        {
            imageUrl = parser.getAttributeValue(null, "url");
            length = parser.getAttributeValue(null, "length");
            imageType = parser.getAttributeValue(null, "type");
        }

        parser.nextTag();

        return imageUrl;
    }

}
