package xh.zero.tadpolestory.test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import xh.zero.core.vo.Status
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.ActivityTestBinding
import xh.zero.tadpolestory.ui.MainViewModel
import xh.zero.tadpolestory.ui.MediaItemData
import xh.zero.tadpolestory.ui.album.AlbumViewModel

@AndroidEntryPoint
class TestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestBinding
    private var mediaItem: MediaItemData? = null
    private val viewModel: AlbumViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

//        binding.btnLoad.setOnClickListener {
//            viewModel.getAlbumsList().observe(this) {
//                if (it.status == Status.SUCCESS) {
//                    Timber.d("getAlbums: ${it.data?.albums?.size}")
//                }
//            }
//        }
    }
}