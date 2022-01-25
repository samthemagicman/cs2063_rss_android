package ca.unb.mobiledev.rss;

import android.Manifest;
import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;


public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    private List<KijijiParser.DataModel> m_items;
    private Context m_context;
    private int selectedIndex = -1;
    private Location m_location = null;


    public ListAdapter(List<KijijiParser.DataModel> items, Context context) {
        this.m_items = items;
        this.m_context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Color Selected Item
        holder.itemView.setBackgroundColor(selectedIndex == position ? Color.RED : Color.TRANSPARENT);

        int currentPos = position;

        if (m_items.isEmpty()) return;

        KijijiParser.DataModel item = m_items.get(position);

        //TODO: - Validation for each item - Maybe these should be in their own
        // methods.

//        boolean isValidForDisplay = item.entryModel.containsKey(RssParserUtilities.GlobalTags.title) &&
//                                    item.entryModel.containsKey(RssParserUtilities.GlobalTags.description) &&
//                                    item.entryModel.containsKey(RssParserUtilities.GlobalTags.enclosure) &&
//                                    item.imageBitmap != null &&
//                                    item.entryModel.containsKey(RssParserUtilities.GlobalTags.price) &&
//                                    item.entryModel.containsKey(RssParserUtilities.GlobalTags.lat) &&
//                                    item.entryModel.containsKey(RssParserUtilities.GlobalTags.lon) &&
//                                    item.entryModel.containsKey(RssParserUtilities.GlobalTags.link);

        // Info
        String title = (String) item.entryModel.get(RssParserUtilities.GlobalTags.title);
        String description = (String) item.entryModel.get(RssParserUtilities.GlobalTags.description);

        // Cost
        String cost = (String) item.entryModel.get(RssParserUtilities.GlobalTags.price);
        String costStr = "Cost: $" + cost.split("\\.")[0]; // Lets get rid of cents.

        // Date
        String date = (String) item.entryModel.get(RssParserUtilities.GlobalTags.dcDate);
        String dateToPrint = "Days: N/A";
        try {
            Date dateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(date);
            SimpleDateFormat a = new SimpleDateFormat("dd/MM/yy");

            Date current = new Date();

            int diffInDays = (int)( (current.getTime() - dateTime.getTime())
                    / (1000 * 60 * 60 * 24) );

            //dateToPrint = a.format(dateTime);
            dateToPrint = "Days: " + diffInDays;

        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Location
        String distance = "Dist: N/A";
        if(m_location != null)
        {
            Location deviceLocation = m_location;
            Location itemLocation = new Location("item");
            itemLocation.setLatitude(Double.parseDouble(item.entryModel.get(RssParserUtilities.GlobalTags.lat).toString()));
            itemLocation.setLongitude(Double.parseDouble(item.entryModel.get(RssParserUtilities.GlobalTags.lon).toString()));

            double dist = deviceLocation.distanceTo(itemLocation)/1000; //distance = km
            String conv = String.format("%.2f", dist);
            distance = "Dist: " + conv + "km";
        }

        // Set our items to display the content
            holder.titleView.setText(title);
            holder.descriptionView.setText(description);
            holder.imageView.setImageBitmap(item.imageBitmap);
            holder.costView.setText(costStr);
            holder.distanceView.setText(distance);
            holder.dateView.setText(dateToPrint);
    }

    public void updateCurrentDeviceLocation(Location location)
    {
        m_location = location;

        //TODO: - make this smarter and only update the requierd components and not the whole view.
        this.notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return m_items.size();
    }

    // Holds each TableViewCell view in memory so it can be added to the list when needed.
    public class ViewHolder extends RecyclerView.ViewHolder{

        // Declare all our items that is in each recycler cell.
        ImageView imageView;
        TextView titleView;
        TextView descriptionView;
        TextView costView;
        TextView distanceView;
        TextView dateView;
        ConstraintLayout parentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Attach the widgets to their Ids
            imageView = itemView.findViewById(R.id.image);
            titleView = itemView.findViewById(R.id.text_title);
            descriptionView = itemView.findViewById(R.id.text_desc);
            costView = itemView.findViewById(R.id.cost_desc);
            distanceView = itemView.findViewById(R.id.dist_desc);
            dateView = itemView.findViewById(R.id.date_desc);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }
}
