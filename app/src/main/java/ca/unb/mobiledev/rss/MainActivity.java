package ca.unb.mobiledev.rss;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity{

    RecyclerView m_urlRecyclerView;

    ArrayList<String> m_urlList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_urlList = loadUrlList();

        m_urlRecyclerView = findViewById(R.id.rss_url_list_recycler_view);

        UrlListAdapter urlListAdapter = new UrlListAdapter(m_urlList, this);
        m_urlRecyclerView.addItemDecoration(new DividerItemDecoration(m_urlRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        m_urlRecyclerView.setAdapter(urlListAdapter);
        m_urlRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button btn = findViewById(R.id.request_button);
        btn.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view) {

                if(m_urlList.isEmpty())
                {
                    Toast.makeText(MainActivity.this, "No urls to load.", Toast.LENGTH_LONG).show();
                }
                else
                {
                    ArrayList<String> rssUrlList = new ArrayList<>();
                    {
                        //rssUrlList.add(url);
                        UrlListAdapter adp = (UrlListAdapter) m_urlRecyclerView.getAdapter();
                        rssUrlList.add(adp.getSelectedUrl());
                    }

                    Intent intent = new Intent(MainActivity.this, ListingActivity.class);
                    intent.putExtra("rssUrlList", rssUrlList);
                    startActivity(intent);
                }
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction)
            {
                int pos = viewHolder.getAdapterPosition();
                String url = m_urlList.get(pos);

                m_urlList.remove(pos);
                saveUrlList(m_urlList);
                m_urlRecyclerView.getAdapter().notifyDataSetChanged();

                Snackbar.make(m_urlRecyclerView, "Restore Deleted Item?", Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        m_urlList.add(pos, url);
                        m_urlRecyclerView.getAdapter().notifyDataSetChanged();
                    }
                }).show();
            }
        }).attachToRecyclerView(m_urlRecyclerView);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri intentData = intent.getData();
        if(intentData != null)
        {
            String newUrl = intentData.toString();
            m_urlList.add(newUrl);
            saveUrlList(m_urlList);

            m_urlRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void saveUrlList(ArrayList<String> urlList)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();

        // Remove the old items
        int oldListCount = sp.getInt("url_list_size", 0);
        for(int i = 0; i < oldListCount; i++)
        {
            editor.remove("url_list_item_" + i).commit();
        }

        // Save the items

        editor.putInt("url_list_size", urlList.size()).commit();
        int count = 0;
        for(String item: urlList)
        {
            editor.putString("url_list_item_" + count++, item).commit();
        }
    }

    private ArrayList<String> loadUrlList()
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        ArrayList<String> newUrlList = new ArrayList<>();

        // Read and add the items to the list.
        int listCount = sp.getInt("url_list_size", 0);
        for(int i = 0; i < listCount; i++)
        {
            String url2 = sp.getString("url_list_item_" + i, "ERROR");
            if(url2 != "ERROR")
                newUrlList.add(url2);
        }

        // Give a fake one for fun!
        if(newUrlList.isEmpty())
            newUrlList.add("https://www.kijiji.ca/rss-srp-tool/gta-greater-toronto-area/tools/k0c110l1700272");

        return newUrlList;
    }


}