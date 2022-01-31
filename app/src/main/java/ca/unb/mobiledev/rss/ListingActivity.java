package ca.unb.mobiledev.rss;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ListingActivity extends AppCompatActivity implements OnTaskCompleted, LocationListener, Response.Listener, Response.ErrorListener {

    private ListAdapter listAdapter;
    private ArrayList<String> m_rssUrlList;
    private BaseItemsPackage m_currentPackage;
    private LocationManager locationManager;

    private Timer updateRssTimer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing);

        m_rssUrlList = getIntent().getStringArrayListExtra("rssUrlList");

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

        //ParseTheRssDataFromUrl(m_rssUrlList.get(0));

        //Lets check for updates on the RssStream and then notify.
        // Do task over and over
        int delay = 0;
        int period = 10000; //10s
        updateRssTimer = new Timer();
        updateRssTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ParseTheRssDataFromUrl(m_rssUrlList.get(0));
            }
        }, delay, period);

        // Get the current location of the device
        locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }

        // TODO: - This is probably not safe. Clear my own permissions and retry with no and see what happens.
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000L,500.0f, this);
        Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateRssTimer.cancel();
        updateRssTimer = null;
        SaveViewingHistoryItems();
    }

    private void initRecyclerView()
    {
        RecyclerView view = findViewById(R.id.recyclerView);
        view.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL));
        listAdapter = new ListAdapter(m_currentPackage, this);
        view.setAdapter(listAdapter);
        view.setLayoutManager(new LinearLayoutManager(this));
    }


    private void RequestImagesInBackground(BaseItemsPackage rssPackage)
    {
        // FIXME: - Need to get images somehow... someway... who knows how? Here or back in the kijiji parsing area.
        // Get the images in the background because they have not been downloaded and processed yet.
        for (BaseItem item : rssPackage.items) {
            if (item.bitmapImage != null) continue; //Dont try to load the image for something that already exists
            ImageParserUtilities.RetrieveImageTask task = new ImageParserUtilities.RetrieveImageTask(item, this);
            task.execute(item.bitmapLink);
        }
    }

    // When the images have been loaded from the webpage,
    // we need to update our recycler view - this is how we do it.
    @Override
    public void onImageDownloadCompleted()
    {
        //TODO: - Return model from this async task. Find the matching object in the list
        // then update the individual componenet, not the whole list.
        listAdapter.notifyDataSetChanged();
    }

    // Location Listeners
    @Override
    public void onLocationChanged(@NonNull Location location)
    {
        try {
            if(listAdapter == null) return;
            if(location == null) return;

            listAdapter.updateCurrentDeviceLocation(location);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        //TODO: - if we dont override this we get terminal exception.
        // maybe we should be doing something with this?
    }

    // Volley Listeners
    private void ParseTheRssDataFromUrl(String url)
    {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, this, this);
        queue.add(stringRequest);
    }

    @Override
    public void onResponse(Object response)
    {
        String rssFeed = (String) response;
        // TODO: - do this in background or we get small delay when counting for updates
        try{
            BaseItemsPackage newPackage = KijijiParser.getItemsFromKijiji(rssFeed);
            RequestImagesInBackground(newPackage);

            if(m_currentPackage == null)
            {
                m_currentPackage = newPackage;
            }
            else
            {
                // Copy over hasViewd and new booleans.
                BaseItemsPackage.FeedUpdateInfo updateInfo = newPackage.UpdateFromAnotherPackage(m_currentPackage);
                m_currentPackage = newPackage;

                if(updateInfo.hasUpdates())
                {
                    Log.d("NEW ITEMS", "NEW ITEMS EMIT NOTIFICATION");

                    //FIXME: - Create string with updates vs new Items
                    String str = "";
                    for(BaseItem item: m_currentPackage.items)
                    {
                        if(item.showIndicator)
                            str = str + item.title + "\n";
                    }

                    DoNotification("New items are available on your RSS feed.");
                }
            }

            ApplyViewHistoryItemContentToCurrentContent();
            listAdapter.setData(m_currentPackage);
            listAdapter.notifyDataSetChanged();


        }
        catch(Exception e)
        {
            //TODO: - Handle correctly with meesage to user.
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error)
    {
        Log.d("VolleyError: " , error.toString());
        //TODO: - do something here like show error text in View
    }


    // Sends system notification to user with info text.
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
}