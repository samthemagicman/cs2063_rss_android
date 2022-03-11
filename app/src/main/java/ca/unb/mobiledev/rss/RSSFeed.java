package ca.unb.mobiledev.rss;
import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class RSSFeed implements Serializable {
    static String TAG = "RSSFeed";
    public String name;
    public String url;
    public int latestItemTime;

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "RSSFeed{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", latestItemTime=" + latestItemTime +
                '}';
    }
}
