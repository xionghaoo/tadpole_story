package xh.zero.tadpolestory.ui.album

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import xh.zero.core.replaceFragment
import xh.zero.core.utils.ToastUtil
import xh.zero.tadpolestory.Configs
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentAlbumDetailBinding
import xh.zero.tadpolestory.replaceFragment
import xh.zero.tadpolestory.repo.data.Album
import xh.zero.tadpolestory.ui.BaseFragment
import xh.zero.tadpolestory.ui.MainFragmentDirections
import xh.zero.tadpolestory.utils.TadpoleUtil
import javax.inject.Inject

@AndroidEntryPoint
class AlbumDetailFragment : BaseFragment<FragmentAlbumDetailBinding>() {

    private val args: AlbumDetailFragmentArgs by navArgs()

    @Inject
    lateinit var albumViewModelFactory: AlbumViewModel.AssistedFactory
    private val viewModel: AlbumViewModel by viewModels {
        AlbumViewModel.provideFactory(albumViewModelFactory, args.album.id.toString())
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.repo.savePlayingAlbum(args.album)
        }
    }

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
        binding.btnBack.setOnClickListener {
            activity?.onBackPressed()
        }
        binding.tvAlbumTitle.text = args.album.album_title
        TadpoleUtil.loadAvatar(context, binding.ivAlbumCover, args.album.cover_url_large.orEmpty())
        binding.tvAlbumDesc.text = args.album.recommend_reason
        binding.tvAlbumSubscribe.text = "订阅量: ${args.album.subscribe_count}"
        val tags = args.album.album_tags.orEmpty().split(",")
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

        binding.btnSubscribe.setOnClickListener {
            ToastUtil.show(context, "订阅")
        }

        binding.vpAlbumDetail.adapter = AlbumDetailAdapter()
        binding.tlAlbumDetail.setViewPager(binding.vpAlbumDetail)
    }

    inner class AlbumDetailAdapter : FragmentPagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val titles = arrayOf("简介", "目录")

        override fun getCount(): Int = 2

        override fun getItem(position: Int): Fragment = if (position == 0) {
            AlbumInfoFragment.newInstance(args.album.album_intro.orEmpty(), args.album.short_rich_intro.orEmpty())
        } else {
            TrackListFragment.newInstance(args.album.id.toString(), args.album.include_track_count, args.album.album_title.orEmpty())
        }

        override fun getPageTitle(position: Int): CharSequence? = titles[position]
    }

    companion object {
        const val ACTION_RECORD_ALBUM = "${Configs.PACKAGE_NAME}.AlbumDetailFragment.ACTION_RECORD_ALBUM"
    }

}