package ca.unb.mobiledev.rss;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.Serializable;
import java.util.List;

public class MainActivity extends AppCompatActivity
{

    Button getResponseButton;
    KijijiParser.KijijiRssPackage kijijiRssPackage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = findViewById(R.id.request_button);
        btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                String url ="https://www.kijiji.ca/rss-srp-fredericton/test/k0l1700018";
                GetItemsFromRssUrlAndLaunchListView(url);
            }
        });
    }

    private void GetItemsFromRssUrlAndLaunchListView(String url)
    {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        try
                        {
                            kijijiRssPackage =  KijijiParser.ParseRssFeed(response);

                            Intent intent = new Intent(MainActivity.this, ListingActivity.class);
                            intent.putExtra("rssItems", (Serializable) kijijiRssPackage);
                            startActivity(intent);
                        }
                        catch (Exception e)
                        {
                            //TODO: - We should inform the user that we couldnt get the
                            // data from the url
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        //TODO: - We should inform the user that we couldnt get the
                        // data from the url
                        assert(false);
                        Log.d("RssNoResponse:", "ERROR");
                    }
                });

        queue.add(stringRequest);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}