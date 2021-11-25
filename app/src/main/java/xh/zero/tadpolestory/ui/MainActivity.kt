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
import xh.zero.tadpolestory.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    private var mediaItem: MediaItemData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        volumeControlStream = AudioManager.STREAM_MUSIC

//        viewModel.getLoginUrl().observe(this) {
//            if (it.status == Status.SUCCESS) {
//                it.data?.login_url?.also { url ->
//                    WebActivity.start(this, url)
//                }
//            }
//        }

        viewModel.mediaItems.observe(this) { list ->
            Timber.d("load media list: ${list.size}")
            if (list.isNotEmpty()) {
                mediaItem = list.first()
            }
        }

        binding.btnMedia.setOnClickListener {
            if (mediaItem != null) {
                Timber.d("media id = ${mediaItem?.mediaId}\ntitle = ${mediaItem?.title}")
                viewModel.playMedia(mediaItem!!, pauseAllowed = false)
            }
        }


    }

    fun onViewClick(v: View) {
//        viewModel.getAlbumsList().observe(this) {
//            if (it.status == Status.SUCCESS) {
//                Timber.d("getAlbums: ${it.data?.albums?.size}")
//            }
//        }

//        viewModel.getCategoriesList().observe(this) {
//            if (it.status == Status.SUCCESS) {
//
//            }
//        }
    }
}