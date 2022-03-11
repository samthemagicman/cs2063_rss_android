package ca.unb.mobiledev.rss;

import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class CheckRSSFeeds extends JobService implements Response.ErrorListener {
    public static final String TAG = "CheckRSSFeeds";

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "onStartJob: Checking RSS feeds");

        check(this);

        return true;
    }

    public void check(Context context) {
        Log.d(TAG, "check: Started");
        RSSFeedManager feedManager = new RSSFeedManager(context);

        ArrayList<RSSFeed> feeds = feedManager.readFeedsFromFile();

        Log.d(TAG, "check: Feed size " + feeds.size());

        for (RSSFeed feed : feeds) {
            Log.d(TAG, "onStartJob: Checking feed " + feed.getName() + " " + feed.getUrl());
            final RSSFeed f = feed;
            KijijiItemPackage pkg = new KijijiItemPackage(feed.url);

            ParsingListener listener = new ParsingListener() {
                @Override
                public void onParsingCompleted(KijijiItemPackage pack) {
                    // TODO: Check time stamps, notify user
                    Log.d(TAG, "Checking: " + f.name + " feed");
                    for (BaseItem item : pack.items) {
                        Log.d(TAG, "onParsingCompleted: " + item.title + " " + item.dateTimestamp.getTime());
                    }
                }
            };

            try
            {
                pkg.ParseRssFeed();
                listener.onParsingCompleted(pkg); // Emit that we got all the data but the images

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (XmlPullParserException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {

        return true;
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }
}
