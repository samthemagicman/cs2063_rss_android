package ca.unb.mobiledev.rss;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.ListView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ca.unb.mobiledev.rss.databinding.ActivityListBinding;

public class ListActivity extends AppCompatActivity {

    private ListAdapter listAdapter;
    private List<KijijiParser.DataModel> m_list;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_list = (List<KijijiParser.DataModel>) getIntent().getSerializableExtra("rssItems");
        initRecyclerView();
    }

    private void initRecyclerView()
    {
        RecyclerView view = findViewById(R.id.recyclerView);
        listAdapter = new ListAdapter(m_list, this);
        view.setAdapter(listAdapter);
        view.setLayoutManager(new LinearLayoutManager(this));
    }
}