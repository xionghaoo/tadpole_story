package xh.zero.tadpolestory.ui

import android.media.AudioManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.forEachIndexed
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lzf.easyfloat.EasyFloat
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import xh.zero.core.startPlainActivity
import xh.zero.core.utils.SystemUtil
import xh.zero.core.utils.ToastUtil
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.ActivityMainBinding
import xh.zero.tadpolestory.handleResponse
import xh.zero.tadpolestory.repo.data.Album
import xh.zero.tadpolestory.test.TestActivity
import xh.zero.tadpolestory.ui.album.AlbumDetailActivity
import xh.zero.tadpolestory.ui.home.RecommendAlbumAdapter

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        EasyFloat.with(this)
            .setDragEnable(false)
            .setGravity(
                Gravity.END or Gravity.BOTTOM,
                0,
                offsetY = -resources.getDimension(R.dimen._34dp).toInt()
            )
            .setLayout(R.layout.float_player_view)
            .registerCallback {
                createResult { b, s, view ->
                    val v = view?.findViewById<CardView>(R.id.float_player_view)
                    val vFloatView = view?.findViewById<CardView>(R.id.v_float_root)
                    if (Build.VERSION.SDK_INT >= 28) {
                        vFloatView?.setOutlineAmbientShadowColor(ContextCompat.getColor(this@MainActivity, R.color.colorAccent));
                        vFloatView?.setOutlineSpotShadowColor(ContextCompat.getColor(this@MainActivity, R.color.colorAccent));
                    }
                    v?.setOnClickListener {
                        ToastUtil.show(this@MainActivity, "点击测试")
//                        val animView = v?.findViewById<View>(R.id.v_test)
                        val animView = vFloatView!!
                        animView.animate()
                            .translationZ(0f)
                            .setUpdateListener {
                            }
                            .withEndAction {
                                vFloatView.elevation = 0f
                                v.animate()
                                    .translationX(v.width.toFloat() - v.height)
                                    .withEndAction {
                                        // 把视图替换掉
                                        vFloatView.animate()
                                            .translationX(30f)
                                            .start()
                                    }
                                    .setDuration(1000)
                                    .start()
                            }
                            .start()
                    }
                }
                touchEvent { view, e ->
                    Timber.d("${e.x}, ${e.y}")
                }
            }
            .show()
    }

}