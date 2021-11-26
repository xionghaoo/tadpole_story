package xh.zero.tadpolestory.ui

import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import xh.zero.core.utils.SystemUtil
import xh.zero.core.vo.Status
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.ActivityMainBinding
import xh.zero.tadpolestory.handleResponse
import xh.zero.tadpolestory.startPlainActivity
import xh.zero.tadpolestory.test.TestActivity

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        volumeControlStream = AudioManager.STREAM_MUSIC

        SystemUtil.setDarkStatusBar(window)

        binding.btnHome.setOnClickListener {
            onBackPressed()
        }

        loadData()
    }

    private fun loadData() {
        viewModel.getTagList().observe(this) {

        }
    }
}