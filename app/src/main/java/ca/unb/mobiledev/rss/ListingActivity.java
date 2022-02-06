package ca.unb.mobiledev.rss;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.location.Location;
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

public class ListingActivity extends AppCompatActivity implements ParsingListener, LocatorListener {

    private ListAdapter m_listAdapter;
    private ArrayList<String> m_rssUrlList;
    private BaseItemsPackage m_currentPackage;
    private Locator m_locationLocator;

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

        Object obj = (Object) findViewById(R.id.menu_mark_all_as_new_button);

        MenuItem spinnerMenuItem =  menu.findItem(R.id.menu_spinner);
        Spinner spinner = (Spinner) spinnerMenuItem.getActionView();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.planets_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

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

            // FIXME: - Be smarter!
            SaveViewingHistoryItems();
            m_listAdapter.notifyDataSetChanged();
        }

        return super.onOptionsItemSelected(item);
    }

    private void initRecyclerView()
    {
        RecyclerView view = findViewById(R.id.recyclerView);
        view.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL));
        m_listAdapter = new ListAdapter(m_currentPackage, this);
        view.setAdapter(m_listAdapter);
        view.setLayoutManager(new LinearLayoutManager(this));
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
        if(m_currentPackage == null)
        {
            m_currentPackage = pack;
        }
        else
        {
            BaseItemsPackage.FeedUpdateInfo updateInfo = pack.UpdateFromAnotherPackage(m_currentPackage);
            m_currentPackage = pack;

            if(updateInfo.hasUpdates())
            {
                DoNotification("New Items Posted!");
            }
        }

        ApplyViewHistoryItemContentToCurrentContent();
        m_listAdapter.setData(m_currentPackage);
        m_listAdapter.notifyDataSetChanged();
    }


    // INFO: - Methods used for the class


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
}