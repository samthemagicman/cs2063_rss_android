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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = findViewById(R.id.request_button);
        btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                String url ="https://www.kijiji.ca/rss-srp-fredericton/test/k0l1700018";
                getRequest(url);
            }
        });
    }

    private void getRequest(String url)
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
                            List<KijijiParser.DataModel> items =  KijijiParser.parseRssFeed(response);
                            Intent intent = new Intent(MainActivity.this, ListActivity.class);
                            intent.putExtra("rssItems", (Serializable) items);
                            startActivity(intent);
                        }
                        catch (Exception e)
                        {
                          e.printStackTrace();
                          assert(false);
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
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