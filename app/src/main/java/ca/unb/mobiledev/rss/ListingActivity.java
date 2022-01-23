package ca.unb.mobiledev.rss;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ListingActivity extends AppCompatActivity implements OnTaskCompleted {

    private ListAdapter listAdapter;
    private List<KijijiParser.DataModel> m_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing);

        m_list = (List<KijijiParser.DataModel>) getIntent().getSerializableExtra("rssItems");

        // Get the background because they have not been processed yet
        for(KijijiParser.DataModel item: m_list)
        {
            KijijiParser.RetrieveImageTask task = new KijijiParser.RetrieveImageTask(item, this);
            task.execute((String)item.entryModel.get(RssParserUtilities.GlobalTags.enclosure));
        }

        initRecyclerView();
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
    public void onTaskCompleted() {
        RecyclerView view = findViewById(R.id.recyclerView);
        view.getAdapter().notifyDataSetChanged();
    }
}