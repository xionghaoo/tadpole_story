package xh.zero.tadpolestory.ui.album

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import xh.zero.core.replaceFragment
import xh.zero.tadpolestory.Configs
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.ActivityAlbumDetailBinding
import xh.zero.tadpolestory.handleResponse
import xh.zero.tadpolestory.ui.BaseActivity
import javax.inject.Inject

@AndroidEntryPoint
class AlbumDetailActivity : BaseActivity() {

    companion object {
        private const val EXTRA_ALBUM_ID = "${Configs.PACKAGE_NAME}.AlbumDetailActivity.EXTRA_ALBUM_ID"
        private const val EXTRA_TOTAL = "${Configs.PACKAGE_NAME}.AlbumDetailActivity.EXTRA_TOTAL"
        private const val EXTRA_ALBUM_TITLE = "${Configs.PACKAGE_NAME}.AlbumDetailActivity.EXTRA_ALBUM_TITLE"

        fun start(context: Context?, albumId: Int, total: Int, albumTitle: String) {
            context?.startActivity(Intent(context, AlbumDetailActivity::class.java).apply {
                putExtra(EXTRA_ALBUM_ID, albumId)
                putExtra(EXTRA_TOTAL, total)
                putExtra(EXTRA_ALBUM_TITLE, albumTitle)
            })
        }
    }

    private lateinit var binding: ActivityAlbumDetailBinding

    private val albumId: Int by lazy {
        intent.getIntExtra(EXTRA_ALBUM_ID, -1)
    }

    private val total: Long by lazy {
        intent.getLongExtra(EXTRA_TOTAL, 0)
    }

    private val albumTitle: String by lazy {
        intent.getStringExtra(EXTRA_ALBUM_TITLE) ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlbumDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        replaceFragment(TrackListFragment.newInstance(albumId.toString(), total, albumTitle), R.id.fragment_container)
    }

}