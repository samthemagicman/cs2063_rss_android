package ca.unb.mobiledev.rss;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UrlListAdapter extends RecyclerView.Adapter<UrlListAdapter.ViewHolder>
{
    ArrayList<String> m_urlList;

    int m_selectedIndex = -1;

    public UrlListAdapter(ArrayList<String> urlList, Context context)
    {
        this.m_urlList = urlList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_url_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull UrlListAdapter.ViewHolder holder, int position)
    {
        holder.urlView.setText(m_urlList.get(position));
        holder.itemView.setBackgroundColor(m_selectedIndex == position ? Color.rgb(62,170,250) : Color.TRANSPARENT);

        //Click Listener - update selection color and launch external webpage.
        holder.parentLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Show item in external browser
                if(m_selectedIndex == position) // User double clicked
                {
                    // TODO: - Emit that the link should be updated? - probably not
                }
                else
                {
                    // Unselect old
                    notifyItemChanged(m_selectedIndex);
                    m_selectedIndex = position;

                    // Select new
                    notifyItemChanged(m_selectedIndex);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return m_urlList.size();
    }

    public String getSelectedUrl()
    {
        if(m_selectedIndex == -1) return null;
        if(m_urlList.isEmpty()) return null;

        return m_urlList.get(m_selectedIndex);
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        //TextView titleView;
        TextView urlView;
        ConstraintLayout parentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //titleView = itemView.findViewById();
            urlView = itemView.findViewById(R.id.rss_url_list_item_textview);
            parentLayout = itemView.findViewById(R.id.url_list_parent_layout);

        }
    }
}
