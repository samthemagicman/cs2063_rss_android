package ca.unb.mobiledev.rss;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class RSSFeedManager {
    private static final String TAG = "RSSFeedManager";
    private static ArrayList<RSSFeed> RSSFeedCache = null;
    private static String filename = "RSSFeeds2";

    private Context context;

    public RSSFeedManager(Context context) {
        this.context = context;
    }

    public ArrayList<RSSFeed> getRSSFeeds() {
        return RSSFeedCache;
    }

    public ArrayList<RSSFeed> readFeedsFromFile() {
        //if (RSSFeedCache == null) {
            try {
                FileInputStream fos = context.openFileInput(filename);
                ObjectInputStream oos = new ObjectInputStream(fos);
                RSSFeedCache = (ArrayList<RSSFeed>) oos.readObject();
                fos.close();
                oos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "ReadSavedFeeds: File not found");
                RSSFeedCache = new ArrayList<RSSFeed>();
            } catch (Exception e) {
                Log.d(TAG, "ReadSavedFeeds: Error reading feed");
                e.printStackTrace();
            }
        //}

        return RSSFeedCache;
    }

    public void saveFeedsToFile() {
        try {
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(RSSFeedCache);
            fos.close();
            oos.close();
        } catch (Exception e) {
            Log.d(TAG, "SaveFeed: Error saving feed");
            e.printStackTrace();
        }
    }

    public void addFeed(RSSFeed feed) {
        RSSFeedCache.add(feed);
    }

    public void removeFeed(RSSFeed feed) {
        RSSFeedCache.remove(feed);
    }
}
