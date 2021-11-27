package xh.zero.tadpolestory.ui.album

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import xh.zero.core.replaceFragment
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.ActivityNowPlayingBinding
import xh.zero.tadpolestory.ui.BaseActivity

@AndroidEntryPoint
class NowPlayingActivity : BaseActivity() {

    private lateinit var binding: ActivityNowPlayingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNowPlayingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
        replaceFragment(NowPlayingFragment.newInstance(), R.id.fragment_container)
    }
}