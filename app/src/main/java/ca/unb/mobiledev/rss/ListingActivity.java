package ca.unb.mobiledev.rss;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class ListingActivity extends AppCompatActivity implements ParsingListener, LocatorListener, OnTaskCompleted {

    private ListAdapter m_listAdapter;
    private RSSFeed m_selectedRssFeed;
    private BaseItemsPackage m_currentPackage;
    private Locator m_locationLocator;
    private RecyclerView m_listView;

    private Timer m_updateRssTimer = null;

    private long m_lastUserInteraction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing);

        m_selectedRssFeed = (RSSFeed) getIntent().getSerializableExtra("selectedRssFeed");
        getSupportActionBar().setTitle(m_selectedRssFeed.name);

        initRecyclerView();

        m_locationLocator = new Locator(this, this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        m_locationLocator.startLocationServiceLoop(10000, 500);

        startRssParsingTimer(0, 10000);
    }

    @Override
    protected void onStop() {
        stopRssParsingTimer();
        m_locationLocator.stopLocationServiceLoop();
        SaveViewingHistory();
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        SaveViewingHistory();
        m_currentPackage = null;
        super.onDestroy();
    }

        @Override
    protected void onPause() {
        SaveViewingHistory();
        super.onPause();
        onStop();
    }

    // INFO: - The following overrides are for the option menu in the toolbar.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_menu, menu);

      //  Object obj = (Object) findViewById(R.id.menu_mark_all_as_new_button);

//        MenuItem spinnerMenuItem =  menu.findItem(R.id.menu_spinner);
//        Spinner spinner = (Spinner) spinnerMenuItem.getActionView();
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.planets_array, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(adapter);

//        spinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
//            {
//
//            }
//        });


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();

        if(id == R.id.menu_mark_all_as_viewed_button || id == R.id.menu_mark_all_as_new_button)
        {
            boolean showIndicator = id == R.id.menu_mark_all_as_new_button;
            for (int i = 0; i < m_currentPackage.items.size(); i++)
            {
                m_currentPackage.items.get(i).showIndicator = showIndicator;
            }

            SaveViewingHistory();
            m_listAdapter.notifyItemRangeChanged(0, m_currentPackage.items.size());
        }

        return super.onOptionsItemSelected(item);
    }

    private void initRecyclerView()
    {
        m_listView = findViewById(R.id.recyclerView);
        m_listView.addItemDecoration(new DividerItemDecoration(m_listView.getContext(), DividerItemDecoration.VERTICAL));
        m_listAdapter = new ListAdapter(m_currentPackage, this);
        m_listView.setAdapter(m_listAdapter);
        m_listView.setLayoutManager(new LinearLayoutManager(this));
        m_listView.setBackgroundColor(Color.WHITE);
    }


    // INFO - VOLLEY
    // The following  are the volley callbacks.
    // Volley is used so we can get the xml data from the RSS server.
    // Abstracting this stuff out would be a considerable amount of work and crazy becasue
    // no one else is using this code.
    private void ParseTheRssDataFromUrl(String url)
    {
        KijijiParser parser = new KijijiParser(this, this);
        parser.getRssFeed(url);
    }

    @Override
    public void onParsingCompleted(KijijiItemPackage pack)
    {
        Log.d("ListingActivity: ", "Got New XML for RSS Feed");

        // If we have no existing data then its all new!
        if(m_currentPackage == null)
        {
            m_currentPackage = pack;
            LoadViewingHistory();
            m_listAdapter.setData(m_currentPackage);
            m_listAdapter.notifyDataSetChanged();

            // Get the images in the background
            for(int i = 0; i < m_currentPackage.items.size(); i++)
            {
                ImageParserUtilities.RetrieveImageTask imageTask = new ImageParserUtilities.RetrieveImageTask(m_currentPackage.items.get(i), this);
            }
        }
        else // Lets determine what is new and update the list accordingly.
        {
            int newItemsCount = m_currentPackage.MergePackage(pack);

            if(newItemsCount > 0)
            {
                Log.d("ListingActivity: ", "New Items Were Posted");
                DoNotification(newItemsCount + " new Items Posted!");

                // Note that all new items are posted at the top of the list.
                // This is good so we can easily determine what is new from 0 to count - 1;

                for(int i = 0; i < newItemsCount; i++) // Lets get the images for the new items in the background.
                {
                    ImageParserUtilities.RetrieveImageTask imageTask = new ImageParserUtilities.RetrieveImageTask(m_currentPackage.items.get(i), this);
                }

                m_listAdapter.updateSelectedIndexBy(newItemsCount);
                m_listAdapter.notifyItemRangeInserted(0, newItemsCount);

                // Autoscroll to top of list if no user interaction for 5 seconds.
                long currentTime = Calendar.getInstance().getTimeInMillis();
                if(currentTime - m_lastUserInteraction >= 5000)
                {
                    m_listView.scrollToPosition(0);
                }
            }
        }

        // 40 Items max - Images will use up all the ram and crash the application
        // TODO: - Smart loading of images. Probably wont do this ever.
        int maxItemsInList = 40;
        if(m_currentPackage.items.size() > maxItemsInList)
        {
            int count = 0;
            for(int i = maxItemsInList - 1; i < m_currentPackage.items.size(); i++)
            {
                m_currentPackage.items.remove(i);
                count += 1;
            }

            m_listAdapter.notifyItemRangeRemoved(maxItemsInList - 1, count);
        }


    }

    private void startRssParsingTimer(int delay, int period)
    {
        m_updateRssTimer = new Timer();
        m_updateRssTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ParseTheRssDataFromUrl(m_selectedRssFeed.url);
            }
        }, delay, period);
    }

    private void stopRssParsingTimer()
    {
        m_updateRssTimer.cancel();
    }


    private void DoNotification(String infoText)
    {
        Notifier notify = new Notifier(this);
        notify.postNotification("RSS Update",
               infoText);
    }


    // Loads the last saved history and try to find out which items have already been viewed.
    private void LoadViewingHistory()
    {
        ArrayList<String> viewedLinks = m_selectedRssFeed.viewedItems;
        if(viewedLinks.size() == 0) return;

        for(BaseItem item: m_currentPackage.items)
        {
            for(String viewedLink: viewedLinks)
            {
                if(item.link.equals(viewedLink))
                {
                    item.showIndicator = false; //User has already seen this item.
                    break;
                }
            }
        }
    }

    // Saves the current viewing history - ie user has already viewed
    // This saves the item.link and compares it on loading.
    private void SaveViewingHistory()
    {
        if(m_selectedRssFeed == null || m_currentPackage == null) return;
        if(m_currentPackage.items == null) return;

        ArrayList<String> viewedLinks = m_selectedRssFeed.viewedItems;
        viewedLinks.clear();

        int i = 0;
        for(BaseItem item: m_currentPackage.items)
        {
            if(item.showIndicator == false) // User has already seen this item;
            {
                viewedLinks.add(item.link);
            }
        }

        RSSFeedManager manager = new RSSFeedManager(this);
        //manager.getRssFeedList();
        manager.saveFeedToFile(m_selectedRssFeed);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            switch (requestCode) {
                default:
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0 &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    {
                        // Permission is granted. Continue the action or workflow
                        // in your app.
                        m_locationLocator.startLocationServiceLoop(10000, 500);

                    }  else {
                        // Explain to the user that the feature is unavailable because
                        // the features requires a permission that the user has denied.
                        // At the same time, respect the user's decision. Don't link to
                        // system settings in an effort to convince the user to change
                        // their decision.
                        Toast.makeText(getApplicationContext(), "Cannot get location - permission denied.", Toast.LENGTH_SHORT);
                    }
                    return;
            }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }

    @Override
    public void onLocationUpdated(Location newLocation)
    {
        m_listAdapter.updateCurrentDeviceLocation(newLocation);
    }

    @Override
    public void onImageDownloadCompleted(BaseItem item)
    {
        //Find matching item and notify the list the item image has been updated.
        int itemIndex = 0;
        if(m_currentPackage != null)
        {
            for(BaseItem currentItem: m_currentPackage.items)
            {
                if(currentItem.isSameItem(item))
                    m_listAdapter.notifyItemChanged(itemIndex);

                itemIndex += 1;
            }
        }
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        m_lastUserInteraction = Calendar.getInstance().getTimeInMillis();
    }
}