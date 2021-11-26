package xh.zero.tadpolestory.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.ActivityDailyRecommendBinding

class DailyRecommendActivity : BaseActivity() {

    private lateinit var binding: ActivityDailyRecommendBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailyRecommendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }
}