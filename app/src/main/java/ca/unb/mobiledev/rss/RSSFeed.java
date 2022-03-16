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
    public long lastCheckedTick;
    public ArrayList<String> viewedItems = new ArrayList<>();

    public RSSFeed(String name, String url, ArrayList<String> viewedItemsList)
    {
        this.name = name;
        this.url = url;
        this.viewedItems = viewedItems;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString()
    {

        return "RSSFeed{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", lastCheckedTick=" + lastCheckedTick +
                ", viewedItems=" + viewedItems.toString() +
                '}';
    }
}
