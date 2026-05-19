package com.slotmachineapp.fancyslotmachine

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_BACK
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.sample.samplewebview.utils.crypt
import com.sample.samplewebview.utils.deCrypt
import com.slotmachineapp.fancyslotmachine.databinding.ActivityWebBinding


class WebActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebBinding
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebBinding.inflate(layoutInflater)
        setContentView(binding.root)
        webView = binding.webView
        setupWebView(binding.webView)


        var text = crypt("https://www.google.com")
        Log.d("crypt", "CRYTPTO: $text ")
        text = deCrypt(text)
        Log.d("crypt", "DECRYTPTO: $text ")

        webView.loadUrl(text)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(webView: WebView) {
        webView.webViewClient = WebViewClient()

        webView.settings.apply {
            javaScriptEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            allowContentAccess = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            setSupportMultipleWindows(true)
        }

        webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webView.settings.safeBrowsingEnabled = true
        }

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack()
                CookieManager.getInstance().flush()
                return true
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}