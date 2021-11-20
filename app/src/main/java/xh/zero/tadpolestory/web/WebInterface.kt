package xh.zero.tadpolestory.web

import android.webkit.JavascriptInterface
import timber.log.Timber

class WebInterface {
    @JavascriptInterface
    fun onAuthComplete(response: String) {
        Timber.tag("WebFragment").d("response: $response")
    }
}