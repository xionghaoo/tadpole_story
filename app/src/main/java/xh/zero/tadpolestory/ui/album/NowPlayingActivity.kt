package xh.zero.tadpolestory.ui.album

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import xh.zero.core.replaceFragment
import xh.zero.tadpolestory.Configs
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.ActivityNowPlayingBinding
import xh.zero.tadpolestory.ui.BaseActivity

@AndroidEntryPoint
class NowPlayingActivity : BaseActivity() {

    companion object {

        const val EXTRA_ALBUM_TITLE = "${Configs.PACKAGE_NAME}.NowPlayingActivity.EXTRA_ALBUM_TITLE"
        const val EXTRA_ALBUM_ID = "${Configs.PACKAGE_NAME}.NowPlayingActivity.EXTRA_ALBUM_ID"

        fun start(context: Context?, albumTitle: String?, id: String) {
            context?.startActivity(Intent(context, NowPlayingActivity::class.java).apply {
                putExtra(EXTRA_ALBUM_TITLE, albumTitle)
                putExtra(EXTRA_ALBUM_ID, id)
            })
        }
    }

    private lateinit var binding: ActivityNowPlayingBinding
    private val albumTitle: String by lazy {
        intent.getStringExtra(EXTRA_ALBUM_TITLE) ?: ""
    }
    private val albumId: String by lazy {
        intent.getStringExtra(EXTRA_ALBUM_ID)  ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.page_enter, R.anim.page_exit)

        binding = ActivityNowPlayingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        replaceFragment(NowPlayingFragment.newInstance(albumTitle, albumId), R.id.fragment_container)
    }

    override fun finish() {
        super.finish()
//        overridePendingTransition(R.anim.page_enter, R.anim.page_exit)
    }

}