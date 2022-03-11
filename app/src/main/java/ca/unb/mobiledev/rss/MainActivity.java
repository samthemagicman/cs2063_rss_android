package ca.unb.mobiledev.rss;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    RecyclerView m_urlRecyclerView;
    ArrayList<RSSFeed> m_urlList = new ArrayList<>();

    RSSFeedManager rssFeeds;

    public static MainActivity currentMainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentMainActivity = this;

        rssFeeds = new RSSFeedManager(this);
        rssFeeds.readFeedsFromFile();


        //region Job Scheduler starting
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(0, new ComponentName(this, CheckRSSFeeds.class));
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setRequiresBatteryNotLow(true);
        }
        builder.setPeriodic(10 * 1000); // This actually only runs every 15 minutes
        builder.setPersisted(true); // So job loads on boot
        jobScheduler.schedule(builder.build());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            boolean isScheduled = jobScheduler.getPendingJob(0) != null;
            Log.d(TAG, "onCreate: IsScheduled " + isScheduled + " " + JobInfo.getMinPeriodMillis());
        }
        //endregion

        //region Quick debugging of job scheduling check function so it runs on app open
        CheckRSSFeeds fe = new CheckRSSFeeds();
        fe.check(this);
        //endregion

        setContentView(R.layout.activity_main);
        m_urlList = rssFeeds.getRSSFeeds();

        if (m_urlList.isEmpty()) {
            RSSFeed newItem = new RSSFeed();
            newItem.url = "https://www.kijiji.ca/rss-srp-tool/gta-greater-toronto-area/tools/k0c110l1700272";
            newItem.name = "Tools";
            rssFeeds.addFeed(newItem);

            m_urlList = rssFeeds.getRSSFeeds();
        }

        m_urlRecyclerView = findViewById(R.id.rss_url_list_recycler_view);

        UrlListAdapter urlListAdapter = new UrlListAdapter(m_urlList, this);
        m_urlRecyclerView.addItemDecoration(new DividerItemDecoration(m_urlRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        m_urlRecyclerView.setAdapter(urlListAdapter);
        m_urlRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // When the "View RSS Feed" button is pressed
        Button btn = findViewById(R.id.request_button);
        btn.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view) {

                if(m_urlList.isEmpty())
                {
                    Toast.makeText(MainActivity.this, "No urls to load.", Toast.LENGTH_LONG).show();
                }
                else
                {
                    RSSFeed selectedFeedItem;
                    ArrayList<String> rssUrlList = new ArrayList<>();
                    {
                        //rssUrlList.add(url);
                        UrlListAdapter adp = (UrlListAdapter) m_urlRecyclerView.getAdapter();

                        selectedFeedItem = adp.getSelectedRSSFeedItem();
                        rssUrlList.add(selectedFeedItem.url);
                    }

                    Intent intent = new Intent(MainActivity.this, ListingActivity.class);
                    intent.putExtra("rssUrlList", rssUrlList);
                    intent.putExtra("rssFeedName", selectedFeedItem.name);
                    startActivity(intent);
                }
            }
        });

        // This is for swiping the url list
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction)
            {
                int pos = viewHolder.getAdapterPosition();
                RSSFeed url = m_urlList.get(pos);

                rssFeeds.removeFeed(url);

                m_urlList = rssFeeds.getRSSFeeds();

                rssFeeds.saveFeedsToFile();

                m_urlRecyclerView.getAdapter().notifyDataSetChanged();

                Snackbar.make(m_urlRecyclerView, "Restore Deleted Item?", Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        m_urlList.add(pos, url);
                        m_urlRecyclerView.getAdapter().notifyDataSetChanged();
                    }
                }).show();
            }
        }).attachToRecyclerView(m_urlRecyclerView);

        // Launch Chrome so user can get more rssfeeds
        FloatingActionButton fab = findViewById(R.id.add_url_floating_action_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent showKijijiIntent = new Intent(MainActivity.this, WebviewSearch.class);
                MainActivity.this.startActivity(showKijijiIntent);
            }
        });
    }

    public void AddToRSSList(String URL, String name) {
        Log.d("MainActivity", "AddToRSSList: " + URL);
        RSSFeed newItem = new RSSFeed();
        newItem.name = name;
        newItem.url = URL;
        rssFeeds.addFeed(newItem); // Adds to RSS feed list

        m_urlList = rssFeeds.getRSSFeeds(); // Just reads cached data

        rssFeeds.saveFeedsToFile(); // Save to file

        m_urlRecyclerView.getAdapter().notifyDataSetChanged();
    }
//
//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//
//        Uri intentData = intent.getData();
//        if(intentData != null)
//        {
//            String newUrl = intentData.toString();
//            m_urlList.add(newUrl);
//            saveUrlList(m_urlList);
//            m_urlRecyclerView.getAdapter().notifyDataSetChanged();
//        }
//    }

    @Override
    protected void onStart() {
        super.onStart();

    }

}