package ca.unb.mobiledev.rss;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

public class WebviewSearch extends AppCompatActivity {
    final String TAG = "WebViewSearch";
    WebView mainWebview;
    String currentRSSUrl;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();

        setContentView(R.layout.webview_search);
        Button saveButton = findViewById(R.id.save);
        saveButton.setEnabled(false);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WebviewSearch.super.onBackPressed();
                Toast.makeText(WebviewSearch.this, "Saved " + currentRSSUrl, Toast.LENGTH_LONG).show();
            }
        });


        mainWebview =  findViewById(R.id.webview);
        WebViewClient webViewClient = new WebViewClient();
        WebSettings webSettings = mainWebview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        Log.d(TAG, "onStart: Loading webview client");


        ProgressBar webviewProgressBar = findViewById(R.id.webviewProgressBar);

        //Exclusively used for the loading bar
        mainWebview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress)
            {
                webviewProgressBar.setVisibility(View.VISIBLE);
                webviewProgressBar.setProgress(progress);

                if(progress == 100)
                    webviewProgressBar.setVisibility(View.INVISIBLE);
            }
        });

        mainWebview.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d(TAG, "onPageStarted: Loading page " + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, "onPageFinished: " + url);

                if (!url.contains("kijiji.ca")) {
                    mainWebview.loadUrl("https://kijiji.ca/");
                } else {
                    // Check for RSS button.
                    mainWebview.evaluateJavascript(
                            "(function() { var element = document.getElementsByClassName(\"rss-link\")[0]; return element.href; })();",
                            new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String html) {
                                    Log.d(TAG, "onReceiveValue: " + html);
                                    if (html.equals("null") || html.equals("undefined")) {
                                        currentRSSUrl = "";
                                        saveButton.setEnabled(false);
                                    } else {
                                        currentRSSUrl = html;
                                        saveButton.setEnabled(true);
                                    }
                                }
                            });
                }


            };
            //Prevents user from going to other pages that aren't kijiji
            @Override
            public boolean shouldOverrideUrlLoading (WebView view, String url) {
                if (url.contains("kijiji.ca")) {
                    Log.d(TAG, "shouldOverrideUrlLoading: Loading continuing");
                    view.loadUrl(url);
                    return true;
                } else {
                    view.loadUrl("https://kijiji.ca/");
                    Log.d(TAG, "shouldOverrideUrlLoading: Loading cancelling");
                    return true;
                }
            };
            @RequiresApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (request.getUrl().toString().contains("kijiji")) {
                    Log.d(TAG, "shouldOverrideUrlLoading: Loading continuing");
                    view.loadUrl(request.getUrl().toString());
                    return true;
                } else {
                    mainWebview.loadUrl("https://kijiji.ca/");
                    Log.d(TAG, "shouldOverrideUrlLoading: Loading cancelling");
                    return true;
                }
            };
        });

        mainWebview.loadUrl("https://kijiji.ca/");
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: ");
        if (mainWebview.canGoBack()) {
            mainWebview.goBack();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
