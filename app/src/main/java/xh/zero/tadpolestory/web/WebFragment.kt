package xh.zero.tadpolestory.web

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.gson.Gson
import okhttp3.*
import timber.log.Timber
import xh.zero.tadpolestory.databinding.FragmentWebBinding
import xh.zero.tadpolestory.repo.data.GetAccessTokenResult
import java.io.IOException
import java.lang.Exception

class WebFragment : Fragment() {
    private lateinit var binding: FragmentWebBinding
    private var url: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            url = it.getString(ARG_URL)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWebBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.webView.settings.javaScriptEnabled = true
//        binding.webView.settings.domStorageEnabled = true

        binding.webView.webChromeClient = MyWebChromeClient()
        binding.webView.webViewClient = MyWebViewClient()
        binding.webView.setBackgroundColor(Color.TRANSPARENT)
        binding.webView.addJavascriptInterface(WebInterface(), "Android")

        if (url != null) {
            binding.webView.loadUrl(url!!)
        }
    }

    inner class MyWebChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            binding.pbWeb.progress = newProgress * 100
            if (newProgress >= 1) {
                binding.pbWeb.visibility = View.GONE
            }
        }
    }

    inner class MyWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            val requestUrl = request?.url?.toString()
            if (requestUrl?.contains("callback/oauth2/get_access_token?code") == true) {
                val req = Request.Builder()
                    .url(requestUrl)
                    .build()
                val client = OkHttpClient()
                client.newCall(req).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Timber.d(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
//                        Timber.d("get_access_token response: ${response.body?.string()}")
                        try {
                            val responseStr = response.body?.string()
                            val gson = Gson()
                            val result = gson.fromJson<GetAccessTokenResult>(responseStr, GetAccessTokenResult::class.java)

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                })
                return true
//                view?.loadUrl("javascript:Android.onAuthComplete(document.getElementsByTagName('pre')[0].innerHTML);")
            }
            view?.loadUrl(request?.url?.toString()!!)
            return false
        }

        override fun onPageFinished(view: WebView?, url: String?) {
//            if (url?.contains("callback/oauth2/get_access_token?code") == true) {
//                Timber.d("webview call native: $view")
//                view?.loadUrl("javascript:Android.onAuthComplete(document.getElementsByTagName('pre')[0].innerHTML);")
//            }
        }
    }

    companion object {
        private const val ARG_URL = "arg_url"

        fun newInstance(url: String) =
            WebFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_URL, url)
                }
            }
    }
}