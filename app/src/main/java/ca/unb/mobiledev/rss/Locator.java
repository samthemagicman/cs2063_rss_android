package ca.unb.mobiledev.rss;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

interface LocatorListener {
    void onLocationUpdated(Location newLocation);
}

/*This class is a helper class for using the location services.
 * We use this to get the current location so we can compute
 * the distance to the item*/
public class Locator implements LocationListener {

    private LocationManager m_locationManager;
    private LocatorListener m_listener;
    private Context m_context;

    public Locator(Context context, LocatorListener listener) {
        m_context = context;
        m_listener = listener;

        m_locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);

        if (!hasPermissions()) {
            requestPermissions();
        }
    }

    public void startLocationServiceLoop(long updateTimeMS, long updateDistanceMeters) {
        if (m_locationManager == null) return;

        if (ActivityCompat.checkSelfPermission(m_context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(m_context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateTimeMS, updateDistanceMeters, this);
        }
    }

    public void stopLocationServiceLoop() {
        m_locationManager.removeUpdates(this);
    }

    private boolean hasPermissions() {
        boolean accessFineLocation = ActivityCompat.checkSelfPermission(m_context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean accessCourseLocation = ActivityCompat.checkSelfPermission(m_context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        return accessFineLocation && accessCourseLocation;
    }

    private void requestPermissions()
    {
        ActivityCompat.requestPermissions((Activity) m_context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
    }

    @Override
    public void onLocationChanged(@NonNull Location location)
    {
            if (ActivityCompat.checkSelfPermission(m_context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(m_context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions();
            }
            Location loc = m_locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            m_listener.onLocationUpdated(loc);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        Log.d("Location Listener", "Status Changed");
        return;
    }
}
