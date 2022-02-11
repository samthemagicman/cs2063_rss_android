package ca.unb.mobiledev.rss;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.location.Location;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ListingActivity extends AppCompatActivity implements ParsingListener, LocatorListener, OnTaskCompleted {

    private ListAdapter m_listAdapter;
    private ArrayList<String> m_rssUrlList;
    private BaseItemsPackage m_currentPackage;
    private Locator m_locationLocator;
    private RecyclerView m_listView;

    private Timer m_updateRssTimer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing);

        m_rssUrlList = getIntent().getStringArrayListExtra("rssUrlList");

        m_locationLocator = new Locator(this, this);

        if(m_rssUrlList.isEmpty())
        {
            //TODO: - Display Message
            Log.d("ERROR", "No rss urls in list,");
            return;
        }

        initRecyclerView();
    }

    @Override
    protected void onStart() {
        super.onStart();

        m_locationLocator.startLocationServiceLoop(10000, 500);

        startRssParsingTimer(0, 10000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopRssParsingTimer();
        m_locationLocator.stopLocationServiceLoop();
        SaveViewingHistoryItems();
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

            SaveViewingHistoryItems();
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
        Log.d("ListingActivity: ", "Got new data");

        // If we have no existing data then its all new!
        if(m_currentPackage == null)
        {
            m_currentPackage = pack;
            ApplyViewHistoryItemContentToCurrentContent();
            m_listAdapter.setData(m_currentPackage);
            m_listAdapter.notifyDataSetChanged();

            // Get the images in the background
            for(int i = 0; i < m_currentPackage.items.size(); i++)
            {
                ImageParserUtilities.RetrieveImageTask imageTask = new ImageParserUtilities.RetrieveImageTask(m_currentPackage.items.get(i), this);
                imageTask.execute(m_currentPackage.items.get(i).bitmapLink);
            }
        }
        else // Lets determine what is new and update the list accordingly.
        {
            int newItemsCount = m_currentPackage.MergePackage(pack);

            if(newItemsCount > 0)
            {
                DoNotification(newItemsCount + " new Items Posted!");

                // Note that all new items are posted at the top of the list.
                // This is good so we can easily determine what is new from 0 to count - 1;

                for(int i = 0; i < newItemsCount; i++) // Lets get the images for the new items in the background.
                {
                    ImageParserUtilities.RetrieveImageTask imageTask = new ImageParserUtilities.RetrieveImageTask(m_currentPackage.items.get(i), this);
                    imageTask.execute(m_currentPackage.items.get(i).bitmapLink);
                }

                m_listAdapter.notifyItemRangeInserted(0, newItemsCount);
            }
        }
    }

    private void startRssParsingTimer(int delay, int period)
    {
        m_updateRssTimer = new Timer();
        m_updateRssTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ParseTheRssDataFromUrl(m_rssUrlList.get(0));
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
    private void ApplyViewHistoryItemContentToCurrentContent()
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        int size = sp.getInt("History_List_Size", 0);

        if(size != 0)
        {
            ArrayList<String> viewedLinks = new ArrayList<>();
            for(int i = 0; i < size; i++)
            {
                viewedLinks.add(sp.getString("Item_" + i, null));
            }

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
    }

    // Saves the current viewing history - ie user has already viewed
    // This saves the item.link and compares it on loading.
    private boolean SaveViewingHistoryItems()
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();

        editor.clear();
        editor.putInt("History_List_Size", m_currentPackage.items.size());

        int i = 0;
        for(BaseItem item: m_currentPackage.items)
        {
            if(item.showIndicator == false) // User has already seen this item;
            {
                editor.putString("Item_" + i++, item.link);
            }
        }

        return editor.commit();
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
        for(BaseItem currentItem: m_currentPackage.items)
        {
            if(currentItem.isSameItem(item))
                m_listAdapter.notifyItemChanged(itemIndex);

            itemIndex += 1;
        }
    }
}