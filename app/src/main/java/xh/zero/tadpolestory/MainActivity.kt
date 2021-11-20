package xh.zero.tadpolestory

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.*
import timber.log.Timber
import xh.zero.core.vo.Status
import xh.zero.tadpolestory.ui.MainViewModel
import xh.zero.tadpolestory.web.WebActivity
import java.io.IOException

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        viewModel.getLoginUrl().observe(this) {
//            if (it.status == Status.SUCCESS) {
//                it.data?.login_url?.also { url ->
//                    WebActivity.start(this, url)
//                }
//            }
//        }


    }

    fun onViewClick(v: View) {
        viewModel.getTagList().observe(this) {
            if (it.status == Status.SUCCESS) {

            }
        }

//        viewModel.getCategoriesList().observe(this) {
//            if (it.status == Status.SUCCESS) {
//
//            }
//        }
    }
}