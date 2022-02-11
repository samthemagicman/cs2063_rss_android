package ca.unb.mobiledev.rss;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;
import android.util.Xml;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Date;

interface ParsingListener
{
    void onParsingCompleted(KijijiItemPackage pack);
}

public class KijijiParser implements Response.Listener, Response.ErrorListener
{
    Context m_context;
    ParsingListener m_listener;
    KijijiItemPackage m_package;
    int m_imagesCompleted = 0;


    public KijijiParser(Context context, ParsingListener listener) {
        this.m_context = context;
        this.m_listener = listener;
    }

    public void getRssFeed(String url)
    {
        RequestQueue queue = Volley.newRequestQueue(m_context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, this, this);
        queue.add(stringRequest);
    }

    public void getItemsFromKijiji(String rssFeedXML)
    {
        m_package = new KijijiItemPackage(rssFeedXML);
        try
        {
            m_package.ParseRssFeed();

            m_listener.onParsingCompleted(m_package); // Emit that we got all the data but the images
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (XmlPullParserException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d("KijijiParser", "Volley Comms Error");
    }

    @Override
    public void onResponse(Object response)
    {
        getItemsFromKijiji( (String) response);
    }
}
