package xh.zero.tadpolestory.ui.album

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import xh.zero.core.replaceFragment
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.ui.BaseActivity

@AndroidEntryPoint
class NowPlayingActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_now_playing)

        replaceFragment(NowPlayingFragment.newInstance(), R.id.fragment_container)
    }
}