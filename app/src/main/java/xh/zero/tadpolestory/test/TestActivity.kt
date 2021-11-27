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

class TestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.skbPlayer.progress = 30
        binding.skbPlayer.secondaryProgress = 60


    }
}