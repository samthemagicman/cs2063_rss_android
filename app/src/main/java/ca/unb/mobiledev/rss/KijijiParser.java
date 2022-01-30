package ca.unb.mobiledev.rss;

import android.graphics.Bitmap;
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
    public static KijijiItemPackage getItemsFromKijiji(String url)
    {
        KijijiItemPackage pack = new KijijiItemPackage(url);
        try {
            pack.ParseRssFeed();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        return pack;
    }
}
