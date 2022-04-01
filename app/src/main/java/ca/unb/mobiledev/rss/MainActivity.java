package ca.unb.mobiledev.rss;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
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
    UrlListAdapter m_urlListAdapter;

    ArrayList<RSSFeed> m_rssFeedList = new ArrayList<>();

   public RSSFeedManager rssFeedManager;

    public static MainActivity currentMainActivity;

    @Override
    protected void onResume()
    {
        super.onResume();

        m_rssFeedList = rssFeedManager.getRssFeedList(true);
        m_urlListAdapter.updateList(m_rssFeedList);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentMainActivity = this;

        rssFeedManager = new RSSFeedManager(this);
        m_rssFeedList = rssFeedManager.getRssFeedList(false);

        setContentView(R.layout.activity_main);

        if (m_rssFeedList == null || m_rssFeedList.isEmpty()) {
            RSSFeed newItem = new RSSFeed("Tools",
                    "https://www.kijiji.ca/rss-srp-tool/gta-greater-toronto-area/tools/k0c110l1700272",
                    new ArrayList<String>());
            rssFeedManager.addFeed(newItem);

            m_rssFeedList = rssFeedManager.getRssFeedList(false);
        }

        m_urlRecyclerView = findViewById(R.id.rss_url_list_recycler_view);
        m_urlRecyclerView.setBackgroundColor(Color.WHITE);

        m_urlListAdapter = new UrlListAdapter(m_rssFeedList, this);
        m_urlRecyclerView.addItemDecoration(new DividerItemDecoration(m_urlRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        m_urlRecyclerView.setAdapter(m_urlListAdapter);
        m_urlRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // When the "View RSS Feed" button is pressed
        Button btn = findViewById(R.id.request_button);
        btn.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view) {

                if(m_rssFeedList.isEmpty())
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
                    intent.putExtra("selectedRssFeed", selectedFeedItem);
                    startActivity(intent);
                }
            }
        });


        // This is for swiping the url list
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction)
            {
                int pos = viewHolder.getAdapterPosition();
                RSSFeed url = m_rssFeedList.get(pos);

                rssFeedManager.removeFeed(url);
                rssFeedManager.saveFeedsToFile();
                m_rssFeedList = rssFeedManager.getRssFeedList(true);
                m_urlListAdapter.updateList(m_rssFeedList);

                Snackbar.make(m_urlRecyclerView, "Restore Deleted Item?", Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        m_rssFeedList.add(pos, url);
                        rssFeedManager.saveFeedsToFile();
                        m_rssFeedList = rssFeedManager.getRssFeedList(true);
                        m_urlListAdapter.updateList(m_rssFeedList);
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

        getSupportActionBar().setTitle("RSS Feed Reader");
    }

    public void AddToRSSList(String URL, String name) {

        String upperString = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        Log.d("MainActivity", "AddToRSSList: " + URL);
        RSSFeed newItem = new RSSFeed(upperString, URL, new ArrayList<String>());

        rssFeedManager.addFeed(newItem); // Adds to RSS feed list
        rssFeedManager.saveFeedsToFile(); // Save to file

        m_rssFeedList = rssFeedManager.getRssFeedList(false);
        m_urlListAdapter.updateList(m_rssFeedList);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

}