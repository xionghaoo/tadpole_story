package xh.zero.tadpolestory.ui

import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.forEachIndexed
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import xh.zero.core.utils.SystemUtil
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.ActivityMainBinding
import xh.zero.tadpolestory.handleResponse
import xh.zero.tadpolestory.repo.data.Album
import xh.zero.tadpolestory.ui.album.AlbumDetailActivity
import xh.zero.tadpolestory.ui.home.RecommendAlbumAdapter

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    private var selectedIndex = 0

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
            handleResponse(it) { r ->
               bindTagList(r.map { tag -> tag.tag_name })
            }
        }

        viewModel.getTemporaryToken().observe(this) {
            handleResponse(it) { r ->
                loadRecommend(r.access_token!!)
            }
        }
    }

    private fun loadRecommend(token: String) {
        viewModel.getDailyRecommendAlbums(token, 1).observe(this) {
            handleResponse(it) { r ->
                bindRecommend(r.albums ?: emptyList())
            }
        }
    }

    private fun bindRecommend(albums: List<Album>) {
        val layout = layoutInflater.inflate(R.layout.item_home_content, null)
        layout.findViewById<TextView>(R.id.tv_album_container_title).text = "每日推荐"
        val rcAlbumList = layout.findViewById<RecyclerView>(R.id.rc_album_list)
        rcAlbumList.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        rcAlbumList.adapter = RecommendAlbumAdapter(albums) { item ->
            AlbumDetailActivity.start(this, item.id)
        }
        binding.llContentList.addView(layout)
    }

    private fun bindTagList(tags: List<String?>) {
        binding.llTagList.removeAllViews()
        tags.forEachIndexed { index, tag ->
            val tv = TextView(this)
            tv.text = tag
            tv.tag = index
            val padding = resources.getDimension(R.dimen._28dp).toInt()
            tv.gravity = Gravity.CENTER
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen._22sp))
            tv.setPadding(padding, 0, padding, 0)
            binding.llTagList.addView(tv)
            val lp = tv.layoutParams as LinearLayout.LayoutParams
            lp.width = LinearLayout.LayoutParams.WRAP_CONTENT
            lp.height = LinearLayout.LayoutParams.MATCH_PARENT
            lp.marginEnd = resources.getDimension(R.dimen._16dp).toInt()

            if (index == selectedIndex) {
                tv.setBackgroundResource(R.drawable.shape_album_tag_selected)
                tv.setTextColor(resources.getColor(R.color.white))
            } else {
                tv.setBackgroundResource(R.drawable.shape_album_tag)
                tv.setTextColor(resources.getColor(R.color.color_42444B))
            }

            selectTagView(tv)

            tv.setOnClickListener { v ->
                selectedIndex = v.tag as Int

                binding.llTagList.children.forEach { child ->
                    selectTagView(child as TextView)
                }
            }
        }
    }

    private fun selectTagView(v: TextView) {
        val tagIndex =  v.tag as Int
        v.apply {
            if (tagIndex == selectedIndex) {
                setBackgroundResource(R.drawable.shape_album_tag_selected)
                setTextColor(resources.getColor(R.color.white))
            } else {
                setBackgroundResource(R.drawable.shape_album_tag)
                setTextColor(resources.getColor(R.color.color_42444B))
            }
        }
    }


}