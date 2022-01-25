package ca.unb.mobiledev.rss;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class ListingActivity extends AppCompatActivity implements OnTaskCompleted, LocationListener {

    private ListAdapter listAdapter;
    private List<KijijiParser.DataModel> m_list;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing);

        m_list = (List<KijijiParser.DataModel>) getIntent().getSerializableExtra("rssItems");

        // Get the images in the background because they have not been downloaded and processed yet.
        for (KijijiParser.DataModel item : m_list) {
            ImageParserUtilities.RetrieveImageTask task = new ImageParserUtilities.RetrieveImageTask(item, this);
            task.execute((String) item.entryModel.get(RssParserUtilities.GlobalTags.enclosure));
        }


        // Get the current location of the device
        locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }

        initRecyclerView();

        // TODO: - This is probably not safe. Clear my own permissions and retry with no and see what happens.
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000L,500.0f, this);
        Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


    }

    private void initRecyclerView()
    {
        RecyclerView view = findViewById(R.id.recyclerView);
        listAdapter = new ListAdapter(m_list, this);
        view.setAdapter(listAdapter);
        view.setLayoutManager(new LinearLayoutManager(this));
    }


    // When the images have been loaded from the webpage,
    // we need to update our recycler view - this is how we do it.
    @Override
    public void onTaskCompleted()
    {
        RecyclerView view = findViewById(R.id.recyclerView);
        //TODO: - Return model from this async task. Find the matching object in the list
        // then update the individual componenet, not the whole list.
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLocationChanged(@NonNull Location location)
    {
        try {
            listAdapter.updateCurrentDeviceLocation(location);
        }
        catch(Exception e)
        {

        }
    }
}