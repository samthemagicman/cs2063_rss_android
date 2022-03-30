package ca.unb.mobiledev.rss;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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

        ArrayList<RSSFeed> feeds = feedManager.getRssFeedList(true);

        Log.d(TAG, "check: Feed size " + feeds.size());

        for (RSSFeed feed : feeds) {
            Log.d(TAG, "onStartJob: Checking feed " + feed.getName() + " " + feed.getUrl());
            final RSSFeed f = feed;
            KijijiItemPackage pkg = new KijijiItemPackage(feed.url);

            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                channel = new NotificationChannel("1", "New Item Notifications", importance);
                channel.setDescription("testing notifications");
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                notificationManager.createNotificationChannel(channel);
            }


            final NotificationCompat.Builder group = new NotificationCompat.Builder(context, "1")
                    .setContentTitle("New items")
                    .setContentText("New items available")
                    .setSmallIcon(R.drawable.common_full_open_on_phone)
                    .setGroup("NewItemNotification")
                    .setGroupSummary(true);

            ParsingListener listener = new ParsingListener() {
                @Override
                public void onParsingCompleted(KijijiItemPackage pack) {
                    // TODO: Check time stamps, notify user

                    BaseItem latestPostedItem = pack.items.get(0);

                    if (latestPostedItem.dateTimestamp.getTime() > f.lastCheckedTick) { // Item was posted more recently than last checked item, so new items are in feed
                        Log.d(TAG, "onParsingCompleted: Notifying user of new items in feed");

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "1")
                                .setSmallIcon(R.drawable.ic_icon)
                                .setContentTitle(feed.name)
                                .setContentText("New Items in " + feed.getName())
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setGroup("NewItemNotification");
                        //TODO: Set on tap action for notification

                        notificationManager.notify(feeds.indexOf(f), builder.build());
                        notificationManager.notify(-1, group.build());
                    }

                    f.lastCheckedTick = latestPostedItem.dateTimestamp.getTime();
                    feedManager.saveFeedToFile(f);

                    /*Log.d(TAG, "onParsingCompleted: " + pack.items.get(0).title + " " + (pack.items.get(0).dateTimestamp.toLocaleString()));
                    for (BaseItem item : pack.items) {
                        Log.d(TAG, "onParsingCompleted: " + item.title + " " + (item.dateTimestamp.toLocaleString()));
                    }*/
                }
            };

            KijijiParser parser = new KijijiParser(context, listener);
            Log.d(TAG, "check URL: " + f.getUrl());
            parser.getRssFeed(f.getUrl());
            /*try
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
            }*/
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
