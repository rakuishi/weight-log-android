package com.rakuishi.weight.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;

import com.rakuishi.weight.R;
import com.rakuishi.weight.databinding.ActivityWebViewBinding;

public class WebViewActivity extends BaseActivity {

    private final static String KEY_URL = "url";
    private ActivityWebViewBinding binding;

    public static Intent create(Context context, String url) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(KEY_URL, url);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_web_view);

        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra(KEY_URL)) {
            throw new IllegalStateException("WebViewActivity requires url parameter.");
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.licenses);
        }

        binding.webView.loadUrl(intent.getStringExtra(KEY_URL));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
