package xh.zero.tadpolestory.ui.album

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import xh.zero.core.utils.ToastUtil
import xh.zero.tadpolestory.Configs
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentAlbumDetailBinding
import xh.zero.tadpolestory.handleResponse
import xh.zero.tadpolestory.repo.data.Album
import xh.zero.tadpolestory.ui.BaseFragment
import xh.zero.tadpolestory.utils.TadpoleUtil
import javax.inject.Inject

@AndroidEntryPoint
class AlbumDetailFragment : BaseFragment<FragmentAlbumDetailBinding>() {

    private val args: AlbumDetailFragmentArgs by navArgs()

    private val viewModel: AlbumDetailViewModel by viewModels()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.repo.savePlayingAlbum(album)
        }
    }
    
    private lateinit var album: Album

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.registerReceiver(receiver, IntentFilter(ACTION_RECORD_ALBUM))
    }

    override fun onDestroy() {
        activity?.unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun onCreateBindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentAlbumDetailBinding {
        return FragmentAlbumDetailBinding.inflate(inflater, container, false)
    }

    override fun rootView(): View = binding.root

    override fun onFirstViewCreated(view: View, savedInstanceState: Bundle?) {
        album = args.album

        viewModel.repo.prefs.nowPlayingAlbumId = album.id.toString()

        binding.btnBack.setOnClickListener {
            activity?.onBackPressed()
        }
        binding.tvAlbumTitle.text = album.album_title
        TadpoleUtil.loadAvatar(context, binding.ivAlbumCover, album.cover_url_large.orEmpty())
        binding.tvAlbumDesc.text = album.recommend_reason
        binding.tvAlbumSubscribe.text = "订阅量: ${album.subscribe_count}"
        val tags = album.album_tags.orEmpty().split(",")
        binding.tvAlbumTags.removeAllViews()
        tags.forEach { tag ->
            val tv = TextView(context)
            tv.setPadding(
                resources.getDimension(R.dimen._20dp).toInt(),
                0,
                resources.getDimension(R.dimen._20dp).toInt(),
                0
            )
            tv.text = tag
            tv.gravity = Gravity.CENTER
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen._16sp))
            tv.setTextColor(resources.getColor(R.color.color_5E5F62))
            tv.background = ContextCompat.getDrawable(requireContext(), R.drawable.shape_album_detail_tag)
            binding.tvAlbumTags.addView(tv)
            val lp = tv.layoutParams as LinearLayout.LayoutParams
            lp.height = LinearLayout.LayoutParams.MATCH_PARENT
            lp.width = LinearLayout.LayoutParams.WRAP_CONTENT
            lp.rightMargin = resources.getDimension(R.dimen._16sp).toInt()
        }

        binding.vpAlbumDetail.adapter = AlbumDetailAdapter()
        binding.tlAlbumDetail.setViewPager(binding.vpAlbumDetail)

        loadIsSubscribe()
    }

    private fun subscribe() {
        viewModel.subscribeAlbum(album.id).observe(this) {
            handleResponse(it) { r ->
                if (r.code == 200 && r.data == true) {
                    ToastUtil.show(context, "订阅成功")
                    loadIsSubscribe()
                } else {
                    ToastUtil.show(context, "订阅失败")
                }
            }
        }
    }

    private fun unsubscribe() {
        viewModel.unsubscribe(album.id).observe(this) {
            handleResponse(it) { r ->
                if (r.code == 200 && r.data == true) {
                    ToastUtil.show(context, "取消订阅")
                    loadIsSubscribe()
                } else {
                    ToastUtil.show(context, "取消订阅失败")
                }
            }
        }
    }

    private fun loadIsSubscribe() {
        viewModel.isSubscribe(album.id).observe(this) {
            handleResponse(it) { r ->
                if (r.data != null) {
                    binding.btnSubscribe.setSubscribe(
                        isSubscribe = r.data,
                        onSubscribeCall = {
                            subscribe()
                        },
                        onUnsubscribeCall = {
                            unsubscribe()
                        }
                    )
                }
            }
        }
    }

    inner class AlbumDetailAdapter : FragmentPagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val titles = arrayOf("简介", "目录")

        override fun getCount(): Int = 2

        override fun getItem(position: Int): Fragment = if (position == 0) {
            AlbumInfoFragment.newInstance(album.album_intro.orEmpty(), album.short_rich_intro.orEmpty())
        } else {
            TrackListFragment.newInstance(album.id.toString(), album.include_track_count, album.album_title.orEmpty())
        }

        override fun getPageTitle(position: Int): CharSequence? = titles[position]
    }

    companion object {
        const val ACTION_RECORD_ALBUM = "${Configs.PACKAGE_NAME}.AlbumDetailFragment.ACTION_RECORD_ALBUM"
    }

}