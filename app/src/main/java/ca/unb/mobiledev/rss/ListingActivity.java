package ca.unb.mobiledev.rss;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.List;

public class ListingActivity extends AppCompatActivity implements OnTaskCompleted, LocationListener {

    private ListAdapter listAdapter;
    private KijijiParser.KijijiRssPackage m_list;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing);

        m_list = (KijijiParser.KijijiRssPackage) getIntent().getSerializableExtra("rssItems");

        RequestImagesInBackground();
        initRecyclerView();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Get the current location of the device
        locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }

        // TODO: - This is probably not safe. Clear my own permissions and retry with no and see what happens.
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000L,500.0f, this);
        Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

    }

    private void initRecyclerView()
    {
        RecyclerView view = findViewById(R.id.recyclerView);
        view.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL));
        listAdapter = new ListAdapter(m_list, this);
        view.setAdapter(listAdapter);
        view.setLayoutManager(new LinearLayoutManager(this));
    }

    private void RequestImagesInBackground()
    {
        // FIXME: - Need to get images somehow... someway... who knows how? Here or back in the kijiji parsing area.
        // Get the images in the background because they have not been downloaded and processed yet.
        for (KijijiParser.KijijiItem item : m_list.items) {
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
}