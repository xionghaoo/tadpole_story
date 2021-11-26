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

        fun start(context: Context?, albumId: Int) {
            context?.startActivity(Intent(context, AlbumDetailActivity::class.java).apply {
                putExtra(EXTRA_ALBUM_ID, albumId)
            })
        }
    }

    private lateinit var binding: ActivityAlbumDetailBinding

    private val albumId: Int by lazy {
        intent.getIntExtra(EXTRA_ALBUM_ID, -1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlbumDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        replaceFragment(TrackListFragment.newInstance(albumId.toString()), R.id.fragment_container)

    }


}