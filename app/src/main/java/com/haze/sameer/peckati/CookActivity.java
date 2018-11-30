package com.haze.sameer.peckati;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

public class CookActivity extends AppCompatActivity {

    ImageView close;
    private WebView webView;
    String loadUrl;
    FrameLayout frameLayout;
    ProgressBar progressBar;
    ImageView buyMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cook);

        loadUrl = getIntent().getStringExtra("recipeUrl");
        close = (ImageView)findViewById(R.id.buy_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        frameLayout = (FrameLayout)findViewById(R.id.frameLoad);
        progressBar = (ProgressBar)findViewById(R.id.proLoad);
        buyMenu = (ImageView)findViewById(R.id.buy_menu);
        progressBar.setMax(100);

        webView = (WebView)findViewById(R.id.buy_webview);
        webView.setWebViewClient(new WebViewClient());

        webView.setWebChromeClient(new WebChromeClient(){
            public void onProgressChanged(WebView view, int progress){
                frameLayout.setVisibility(View.VISIBLE);
                progressBar.setProgress(progress);
                if (progress==100){
                    frameLayout.setVisibility(View.GONE);
                    setTitle(view.getTitle());
                }
                super.onProgressChanged(view,progress);
            }
        });

        webView.loadUrl(loadUrl);
        progressBar.setProgress(0);

        buyMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(CookActivity.this,buyMenu);
                popupMenu.getMenuInflater().inflate(R.menu.menu_main,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.item1:
                                setClipboard(CookActivity.this,webView.getUrl()+"");
                                return true;
                            case R.id.item2:
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, "Peckati App\n"+webView.getUrl());
                                sendIntent.setType("text/plain");
                                startActivity(sendIntent);
                                return true;
                            case R.id.item3:
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webView.getUrl()+""));
                                startActivity(browserIntent);
                                return true;
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

    }

    private void setClipboard(Context context, String text) {

        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }
        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

}
