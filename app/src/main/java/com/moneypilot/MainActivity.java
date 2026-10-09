package com.moneypilot;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.provider.MediaStore;
import android.content.ContentValues;
import android.os.Environment;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.util.Base64;
import android.widget.Toast;
import java.io.OutputStream;
import java.io.IOException;

public class MainActivity extends Activity {
    private WebView webView;
    private ValueCallback<Uri[]> fileChooserCallback;
    private String pendingQuickKind = null;
    private boolean pageReady = false;
    private static final int FILE_CHOOSER_REQUEST = 721;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(android.graphics.Color.rgb(247,249,255));
        getWindow().setNavigationBarColor(android.graphics.Color.rgb(247,249,255));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) getWindow().getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) getWindow().getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        webView = new WebView(this);
        setContentView(webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        webView.addJavascriptInterface(new NativeBridge(), "MoneyPilotNative");
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return !"file".equalsIgnoreCase(request.getUrl().getScheme());
            }
            @Override public void onPageFinished(WebView view, String url) {
                pageReady = true;
                runPendingQuickEntry();
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileChooserCallback != null) fileChooserCallback.onReceiveValue(null);
                fileChooserCallback = callback;
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                try { startActivityForResult(intent, FILE_CHOOSER_REQUEST); }
                catch (Exception e) { fileChooserCallback = null; callback.onReceiveValue(null); return false; }
                return true;
            }
        });
        handleIntent(getIntent());
        webView.loadUrl("file:///android_asset/www/index.html");
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
        runPendingQuickEntry();
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            String kind = intent.getStringExtra("quick_entry_kind");
            if ("expense".equals(kind) || "income".equals(kind)) pendingQuickKind = kind;
        }
    }

    private void runPendingQuickEntry() {
        if (!pageReady || pendingQuickKind == null || webView == null) return;
        final String kind = pendingQuickKind;
        pendingQuickKind = null;
        String js = "(function(){var b=document.getElementById('widgetToggle');var p=document.getElementById('quickWidget');if(b&&p&&!p.classList.contains('open'))b.click();setTimeout(function(){var f=document.getElementById('widgetForm');if(f){f.elements.kind.value='" + kind + "';f.elements.kind.dispatchEvent(new Event('change',{bubbles:true}));f.elements.amount.focus();}},250);})();";
        webView.evaluateJavascript(js, null);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST && fileChooserCallback != null) {
            Uri[] results = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
            fileChooserCallback.onReceiveValue(results);
            fileChooserCallback = null;
        }
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }

    public class NativeBridge {
        @JavascriptInterface public void setWidgetStyle(String style) {
            if (!"aurora".equals(style) && !"mint".equals(style) && !"peach".equals(style) && !"minimal".equals(style) && !"glass".equals(style)) return;
            getSharedPreferences("moneypilot_widget", MODE_PRIVATE).edit().putString("style", style).apply();
            runOnUiThread(() -> {
                AppWidgetManager manager = AppWidgetManager.getInstance(MainActivity.this);
                ComponentName component = new ComponentName(MainActivity.this, MoneyPilotWidgetProvider.class);
                int[] ids = manager.getAppWidgetIds(component);
                if (ids != null && ids.length > 0) new MoneyPilotWidgetProvider().onUpdate(MainActivity.this, manager, ids);
            });
        }
        @JavascriptInterface public void downloadFile(String filename, String mimeType, String base64Data) {
            try {
                byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, filename.replaceAll("[^A-Za-z0-9._-]", "_"));
                values.put(MediaStore.Downloads.MIME_TYPE, mimeType == null ? "application/octet-stream" : mimeType);
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/MoneyPilot");
                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) throw new IOException("Could not create download file");
                try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                    if (out == null) throw new IOException("Could not open download file");
                    out.write(bytes);
                }
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Saved to Downloads/MoneyPilot", Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Download failed. Export from browser or try again.", Toast.LENGTH_LONG).show());
            }
        }
    }
}
