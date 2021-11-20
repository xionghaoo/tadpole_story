package xh.zero.tadpolestory.web

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import xh.zero.core.replaceFragment
import xh.zero.tadpolestory.Configs
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.ActivityWebBinding

class WebActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_URL = "${Configs.PACKAGE_NAME}.WebActivity.EXTRA_URL"

        fun start(context: Context?, url: String?) {
            val intent = Intent(context, WebActivity::class.java)
            intent.putExtra(EXTRA_URL, url)
            context?.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityWebBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.getStringExtra(EXTRA_URL)?.also {
            replaceFragment(WebFragment.newInstance(it), R.id.web_fragment_container)
//            binding.webView.settings.javaScriptEnabled = true
//            binding.webView.settings.domStorageEnabled = true
//
//            binding.webView.webChromeClient = MyWebChromeClient()
//
//            binding.webView.loadUrl(it)
        }
    }

//    inner class MyWebChromeClient : WebChromeClient() {
//        override fun onProgressChanged(view: WebView?, newProgress: Int) {
//            binding.pbWeb.progress = newProgress * 100
//            if (newProgress >= 1) {
//                binding.pbWeb.visibility = View.GONE
//            }
//        }
//    }

}
