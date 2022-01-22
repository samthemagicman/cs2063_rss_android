package ca.unb.mobiledev.rss;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder>
{
    private List<KijijiParser.DataModel> m_items;
    private Context m_context;
    private int selectedIndex = -1;


    public ListAdapter(List<KijijiParser.DataModel> items, Context context)
    {
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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        // Color Selected Item
        holder.itemView.setBackgroundColor(selectedIndex == position ?  Color.RED : Color.TRANSPARENT);

        int currentPos = position;

        KijijiParser.DataModel selectedItem = m_items.get(position);


        holder.titleView.setText(selectedItem.entryModel.get(RssParserUtilities.GlobalTags.title).toString());
        //FIXME: - Add the rest of the items here.
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
        RelativeLayout parentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Attach the widgets to their Ids
            imageView = itemView.findViewById(R.id.image);
            titleView = itemView.findViewById(R.id.text_title);
            descriptionView = itemView.findViewById(R.id.text_desc);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }
}
