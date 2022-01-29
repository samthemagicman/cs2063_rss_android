package ca.unb.mobiledev.rss;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ListingActivity extends AppCompatActivity implements OnTaskCompleted, LocationListener, Response.Listener, Response.ErrorListener {

    private ListAdapter listAdapter;
    private ArrayList<String> m_rssUrlList;
    private KijijiParser.KijijiRssPackage m_currentPackage;
    private LocationManager locationManager;

    private Timer updateRssTimer;

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
    }

    private void initRecyclerView()
    {
        RecyclerView view = findViewById(R.id.recyclerView);
        view.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL));
        listAdapter = new ListAdapter(m_currentPackage, this);
        view.setAdapter(listAdapter);
        view.setLayoutManager(new LinearLayoutManager(this));
    }



    private void RequestImagesInBackground(KijijiParser.KijijiRssPackage rssPackage)
    {
        // FIXME: - Need to get images somehow... someway... who knows how? Here or back in the kijiji parsing area.
        // Get the images in the background because they have not been downloaded and processed yet.
        for (KijijiParser.KijijiItem item : rssPackage.items) {
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
            KijijiParser.KijijiRssPackage newPackage = KijijiParser.ParseRssFeed(rssFeed);

            RequestImagesInBackground(newPackage);

            if(m_currentPackage == null)
            {
                m_currentPackage = newPackage;
                listAdapter.setData(m_currentPackage);
                listAdapter.notifyDataSetChanged();
            }
            else
            {
                // Lets see if the time was updated
                Date currentPackageDate = m_currentPackage.feedPublicationDate;
                Date newPackageDate = newPackage.feedPublicationDate;

                long deltaTime = newPackageDate.getTime() - currentPackageDate.getTime();

                if(deltaTime > 0.0)
                {
                    int newItemCount = 0;
                    // Lets check all our samples for new items
                    for(KijijiParser.KijijiItem newItem: newPackage.items)
                    {
                        boolean hasMatch = false;
                        for(KijijiParser.KijijiItem oldItem: m_currentPackage.items)
                        {
                            hasMatch = newItem.compareTo(oldItem) == 0;
                            if(hasMatch) break;
                        }
                        if(!hasMatch) newItemCount += 1;
                    }

                    if(newItemCount > 0)
                    {
                        Log.d("NEW ITEMS", "NEW ITEMS EMIT NOTIFICATION");
                        listAdapter.notifyDataSetChanged();
                    }

                    //TODO: - Add last updated time to top of Activity window in Seconds.
                    m_currentPackage = newPackage;
                    // We have a new package and should notify the user
                    // Update current table.
                }
            }
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
}