package ca.unb.mobiledev.rss;

import androidx.appcompat.app.AppCompatActivity;

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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity{
    Button getResponseButton;

    BaseItemsPackage m_currentPackage;

    String m_url = "https://www.kijiji.ca/rss-srp-tool/gta-greater-toronto-area/tools/k0c110l1700272";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_url = loadUrl();
        TextView linkTextView = findViewById(R.id.main_activity_link_text_view);
        linkTextView.setText(m_url);

        Button btn = findViewById(R.id.request_button);
        btn.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view) {
               // String url ="https://www.kijiji.ca/rss-srp-fredericton/test/k0l1700018";
               // String urlToronto = "https://www.kijiji.ca/rss-srp-tool/gta-greater-toronto-area/tools/k0c110l1700272";
                ArrayList<String> rssUrlList = new ArrayList<>();
                {
                    //rssUrlList.add(url);
                    rssUrlList.add(m_url);
                }

                Intent intent = new Intent(MainActivity.this, ListingActivity.class);
                intent.putExtra("rssUrlList", rssUrlList);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri intentData = intent.getData();
        if(intentData != null)
        {
            m_url = intentData.toString();
            saveUrl(m_url);
        }

        TextView linkTextView = findViewById(R.id.main_activity_link_text_view);
        linkTextView.setText(m_url);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void saveUrl(String url)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();

        editor.remove("Saved_Kijiji_Url").commit();
        editor.putString("Saved_Kijiji_Url", m_url).commit();
    }

    private String loadUrl()
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String url = sp.getString("Saved_Kijiji_Url", "ERROR");

        return url;
    }


}