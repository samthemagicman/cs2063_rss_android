package ca.unb.mobiledev.rss;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;


public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    private BaseItemsPackage kijijiPackage;
    private Context m_context;
    private int selectedIndex = -1;
    private Location m_currentDeviceLocation = null;


    public ListAdapter(BaseItemsPackage kijijiPackage, Context context) {
        this.kijijiPackage = kijijiPackage;
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
        holder.itemView.setBackgroundColor(selectedIndex == position ? Color.rgb(62,170,250) : Color.TRANSPARENT);

        int currentPos = position;

        if (kijijiPackage.items.isEmpty()) return;

        BaseItem item = kijijiPackage.items.get(position);

        // Cost
        String cost = item.price;
        String costStr = "Cost: $" + cost.split("\\.")[0]; // Lets get rid of cents.

        // Days Posted
        Date current = new Date();
        Date itemPostedDate = item.dateTimestamp;
        int diffInDays = (int)(current.getTime() - itemPostedDate.getTime()) / ((1000 * 60 * 60 * 24));
        String daysToPrint = "Days: " + diffInDays;

        //Distance To Item
        Location itemLocation = new Location("ItemLocation");
        itemLocation.setLatitude(item.lat);
        itemLocation.setLongitude(item.lon);
        Location deviceLocation = m_currentDeviceLocation;
        double dist = (deviceLocation != null) ? deviceLocation.distanceTo(itemLocation) / 1000 : 0.0 ;//dist in km
        String distToPrint = "Dist: " + String.format("%.2f", dist) + "km";

        // Set our items to display the content
            holder.titleView.setText(item.title);
            holder.descriptionView.setText(item.description);
            holder.imageView.setImageBitmap(item.bitmapImage);
            holder.costView.setText(costStr);
            holder.distanceView.setText(distToPrint);
            holder.dateView.setText(daysToPrint);

            boolean shouldShowNotViewedIcon = item.isUpdated || !item.userHasViewed;
            holder.updateNotificationIcon.setVisibility((shouldShowNotViewedIcon) ? View.VISIBLE : View.INVISIBLE);


        //Click Listener - update selection color and launch external webpage.
        holder.parentLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Show item in external browser
                if(selectedIndex == currentPos) // User double clicked
                {
                    ShowExternalBrowser(item.link);
                }

                item.userHasViewed = true;

                notifyItemChanged(selectedIndex);
                selectedIndex = currentPos;
                notifyItemChanged(selectedIndex);

                //TODO: - Switch text to white when selected.
            }
        });
    }

    public void ShowExternalBrowser(String externalUrl)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(externalUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.android.chrome");

        try{
            m_context.startActivity(intent);
        }
        catch(Exception e)
        {
            //Chrome not installed allow user to choose
            intent.setPackage(null);
            m_context.startActivity(intent);
        }
    }

    public void updateCurrentDeviceLocation(Location location)
    {
        m_currentDeviceLocation = location;

        //TODO: - make this smarter and only update the requierd components and not the whole view.
        this.notifyDataSetChanged();
    }

    public void setData(BaseItemsPackage data)
    {
        kijijiPackage = data;
    }


    @Override
    public int getItemCount()
    {
        if(kijijiPackage == null) return 0;

        return kijijiPackage.items.size();
    }

    // Holds each TableViewCell view in memory so it can be added to the list when needed.
    public class ViewHolder extends RecyclerView.ViewHolder{

        // Declare all our items that is in each recycler cell.
        ImageView imageView;
        ImageView updateNotificationIcon;
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
            updateNotificationIcon = itemView.findViewById(R.id.update_notification_icon);
            titleView = itemView.findViewById(R.id.text_title);
            descriptionView = itemView.findViewById(R.id.text_desc);
            costView = itemView.findViewById(R.id.cost_desc);
            distanceView = itemView.findViewById(R.id.dist_desc);
            dateView = itemView.findViewById(R.id.date_desc);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }
}
