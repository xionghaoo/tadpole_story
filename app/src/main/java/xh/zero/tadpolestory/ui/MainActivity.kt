package xh.zero.tadpolestory.ui

import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import xh.zero.core.vo.Status
import xh.zero.tadpolestory.R

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        volumeControlStream = AudioManager.STREAM_MUSIC

//        viewModel.getLoginUrl().observe(this) {
//            if (it.status == Status.SUCCESS) {
//                it.data?.login_url?.also { url ->
//                    WebActivity.start(this, url)
//                }
//            }
//        }



    }

    fun onViewClick(v: View) {
//        viewModel.getAlbumsList().observe(this) {
//            if (it.status == Status.SUCCESS) {
//                Timber.d("getAlbums: ${it.data?.albums?.size}")
//            }
//        }

        viewModel.mediaItems.observe(this) { list ->
            Timber.d("load media list: ${list.size}")
        }

//        viewModel.getCategoriesList().observe(this) {
//            if (it.status == Status.SUCCESS) {
//
//            }
//        }
    }
}