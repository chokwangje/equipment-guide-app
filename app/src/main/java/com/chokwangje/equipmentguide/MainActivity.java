package com.chokwangje.equipmentguide;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends Activity {
    private WebView web;
    private ValueCallback<Uri[]> fileCallback;
    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<Void> takePicturePreview;
    private ActivityResultLauncher<String> pickImage;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), ok -> { if (ok) takePicturePreview.launch(null); else cancelFile(); });
        takePicturePreview = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bmp -> {
            if (bmp == null) { cancelFile(); return; }
            try { Uri u = bitmapToUri(bmp); if (fileCallback != null) fileCallback.onReceiveValue(new Uri[]{u}); }
            catch (Exception e) { cancelFile(); }
            fileCallback = null;
        });
        pickImage = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (fileCallback != null) fileCallback.onReceiveValue(uri == null ? null : new Uri[]{uri});
            fileCallback = null;
        });

        web = new WebView(this);
        setContentView(web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setAllowFileAccess(true); s.setMediaPlaybackRequiresUserGesture(false);
        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient(){
            @Override public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams p) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = cb;
                boolean camera = p != null && p.isCaptureEnabled();
                if (camera) openCamera(); else pickImage.launch("image/*");
                return true;
            }
        });
        web.loadUrl("file:///android_asset/index.html");
    }

    private void openCamera(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) takePicturePreview.launch(null);
        else permissionLauncher.launch(Manifest.permission.CAMERA);
    }
    private Uri bitmapToUri(Bitmap bmp) throws Exception {
        File f = new File(getCacheDir(), "capture_"+System.currentTimeMillis()+".jpg");
        FileOutputStream os = new FileOutputStream(f); bmp.compress(Bitmap.CompressFormat.JPEG, 95, os); os.close();
        return androidx.core.content.FileProvider.getUriForFile(this, getPackageName()+".fileprovider", f);
    }
    private void cancelFile(){ if(fileCallback!=null) fileCallback.onReceiveValue(null); fileCallback=null; }
    @Override public void onBackPressed(){ if(web.canGoBack()) web.goBack(); else super.onBackPressed(); }
}
