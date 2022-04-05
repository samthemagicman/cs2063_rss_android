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
    private static ArrayList<RSSFeed> m_rssFeedList;
    private static String filename = "RSSFeeds2";

    private Context context;

    public RSSFeedManager(Context context) {
        this.context = context;
        readFeedsFromFile();
    }

    public ArrayList<RSSFeed> getRssFeedList(boolean readFromFile)
    {
        if(readFromFile)
            readFeedsFromFile();

        return m_rssFeedList;
    }

    private void readFeedsFromFile() {
            try {
                FileInputStream fos = context.openFileInput(filename);
                ObjectInputStream oos = new ObjectInputStream(fos);
                m_rssFeedList = (ArrayList<RSSFeed>) oos.readObject();
                fos.close();
                oos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "ReadSavedFeeds: File not found");
                m_rssFeedList = new ArrayList<RSSFeed>();
            } catch (Exception e) {
                /*
                There's an error that can happen if we update the RSSFeed class
                where the function tries to use oos.readObject and spits out an error
                because the classes are no longer the same
                 */
                Log.d(TAG, "ReadSavedFeeds: Error reading feed");
                m_rssFeedList = new ArrayList<RSSFeed>();
                e.printStackTrace();
                m_rssFeedList = new ArrayList<RSSFeed>();
            }
    }

    public void saveFeedsToFile() {
        try {
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(m_rssFeedList);
            fos.close();
            oos.close();
        } catch (Exception e) {
            Log.d(TAG, "SaveFeed: Error saving feed");
            e.printStackTrace();
        }
    }

    public void saveFeedToFile(RSSFeed feedItem)
    {
        int index = 0;
        boolean hasMatch = false;
        for(RSSFeed item: m_rssFeedList)
        {
            if(item.url.compareTo(feedItem.url) == 0)
            {
                hasMatch = true;
                break;
            }
            index += 1;
        }

        // Remove the item and add the new one back.
        if(hasMatch)
        {
            m_rssFeedList.remove(index);
        }

        m_rssFeedList.add(index, feedItem);

        saveFeedsToFile();
    }

    public void addFeed(RSSFeed feed)
    {
        m_rssFeedList.add(feed);
    }

    public void removeFeed(RSSFeed feed) {
        m_rssFeedList.remove(feed);
    }
}
