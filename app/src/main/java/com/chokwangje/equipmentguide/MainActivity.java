package com.chokwangje.equipmentguide;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import java.io.ByteArrayOutputStream;

public class MainActivity extends ComponentActivity {
    private WebView webView;
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<Void> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            bitmap -> {
                if (bitmap == null || webView == null) return;
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, os);
                String data = "data:image/jpeg;base64," +
                    Base64.encodeToString(os.toByteArray(), Base64.NO_WRAP);
                String quoted = org.json.JSONObject.quote(data);
                webView.post(() -> webView.evaluateJavascript(
                    "window.receiveNativePhoto(" + quoted + ");", null));
            });

        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> { if (granted) cameraLauncher.launch(null); });

        webView = new WebView(this);
        setContentView(webView);
        WebSettings s=webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setBuiltInZoomControls(false);
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new AndroidBridge(),"Android");
        webView.loadUrl("file:///android_asset/index.html");
    }

    public class AndroidBridge {
        @JavascriptInterface
        public void openCamera() {
            runOnUiThread(() -> {
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) cameraLauncher.launch(null);
                else permissionLauncher.launch(Manifest.permission.CAMERA);
            });
        }
    }

    @Override public void onBackPressed() {
        if(webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}