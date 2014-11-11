package fr.asapp.webapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class WebActivity extends ActionBarActivity {
    final Activity activity = this;
    private WebView myWebView;
    private SharedPreferences myPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_web);
    }

    @Override
    protected void onStart () {
        myPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (!myPrefs.contains("html_url")) {
            startActivity(new Intent(WebActivity.this, SettingsActivity.class));
        }
        myWebView = WebViewSetup(R.id.webview, myPrefs);
        super.onStart();
    }

    public class MyWebViewClient extends WebViewClient {
//        @Override
//        public void onPageStarted(WebView view, String url, Bitmap favicon) {
//            super.onPageStarted(view, url, favicon);
//        }

//        @Override
//        public void onPageFinished(WebView view, String url) {
//            super.onPageFinished(view, url);
//        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (Uri.parse(url).getHost().equals(Uri.parse(myPrefs.getString("html_url", "")).getHost()) || !myPrefs.getBoolean("html_external", true)) {
                // This is my website, so do not override; let my WebView load the page
                return false;
            }
            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }

        @Override
        public void onReceivedError(final WebView webview, int errorCode, String description, String failingUrl) {
            super.onReceivedError(webview, errorCode, description, failingUrl);

            new AlertDialog.Builder(WebActivity.this)
                    .setTitle(R.string.no_app_ask)
                    .setMessage(String.format(getResources().getString(R.string.no_app_description), description))
                    .setPositiveButton(R.string.pref_title_system_network_settings, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                        }
                    })
                    .setNeutralButton(R.string.title_activity_settings, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(WebActivity.this, SettingsActivity.class));
                        }
                    })
                    .setNegativeButton(R.string.action_refresh, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            webview.reload();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            activity.setProgress(newProgress * 1000);
        }

//        @Override
//        public void onGeolocationPermissionsShowPrompt (String origin, GeolocationPermissions.Callback callback) {
//              callback.invoke(origin,true,false);
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.web, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_sync:
                WebViewRefresh(myWebView);
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack()) {
            myWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public WebView WebViewSetup (Integer webview_id, SharedPreferences sharedPref) {

        WebView webview = (WebView) findViewById(webview_id);

        webview.setWebViewClient(new MyWebViewClient());
        webview.setWebChromeClient(new MyWebChromeClient());

        webview.getSettings().setJavaScriptEnabled(sharedPref.getBoolean("html_javascript", false));

        webview.getSettings().setDomStorageEnabled(sharedPref.getBoolean("html_domstorage", false));

        webview.getSettings().setDatabaseEnabled(sharedPref.getBoolean("html_localstorage", false));
        webview.getSettings().setDatabasePath(getApplicationContext().getCacheDir().getAbsolutePath());

        webview.getSettings().setAppCacheEnabled(sharedPref.getBoolean("html_appcache", false));
        webview.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
        webview.getSettings().setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());

//        TODO implement Geolocation,
//        webview.getSettings().setGeolocationEnabled(sharedPref.getBoolean("html_geolocation", false));
//        webview.getSettings().setGeolocationDatabasePath(getApplicationContext().getCacheDir().getAbsolutePath());
//        TODO add to manifest ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION;

//        TODO Check if setAllowFileAccess(true) needed ?!
        webview.getSettings().setAllowFileAccess(true);

        webview.loadUrl(sharedPref.getString("html_url", ""));
        return webview;
    }

    public void WebViewClear(WebView webview){
        webview.clearCache(true);
        //WebStorage.deleteAllData();
        //WebStorage.deleteOrigin(String origin);
    }

    public void WebViewRefresh(WebView webview) {
        webview.clearCache(false);
        webview.reload();
    }

}
