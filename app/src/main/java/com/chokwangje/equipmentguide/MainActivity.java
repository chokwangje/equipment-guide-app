package com.chokwangje.equipmentguide;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

public class MainActivity extends ComponentActivity {
    private WebView webView;
    private ActivityResultLauncher<String> cameraPermission;
    private ActivityResultLauncher<Void> takePicturePreview;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        takePicturePreview = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            bmp -> {
                if (bmp != null && webView != null) {
                    java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                    bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 92, out);
                    String b64 = android.util.Base64.encodeToString(out.toByteArray(), android.util.Base64.NO_WRAP);
                    webView.post(() -> webView.evaluateJavascript(
                        "window.receiveNativePhoto && window.receiveNativePhoto('data:image/jpeg;base64," + b64 + "')", null));
                }
            });

        cameraPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            ok -> { if (ok) takePicturePreview.launch(null); });

        webView = new WebView(this);
        setContentView(webView);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new Bridge(), "Android");
        webView.loadUrl("file:///android_asset/index.html");
    }

    public class Bridge {
        @JavascriptInterface public void openCamera() {
            runOnUiThread(() -> {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    takePicturePreview.launch(null);
                } else cameraPermission.launch(Manifest.permission.CAMERA);
            });
        }
    }
}