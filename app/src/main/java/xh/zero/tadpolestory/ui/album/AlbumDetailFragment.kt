package xh.zero.tadpolestory.ui.album

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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import xh.zero.core.replaceFragment
import xh.zero.core.utils.ToastUtil
import xh.zero.tadpolestory.Configs
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentAlbumDetailBinding
import xh.zero.tadpolestory.replaceFragment
import xh.zero.tadpolestory.ui.MainFragmentDirections
import xh.zero.tadpolestory.utils.TadpoleUtil

class AlbumDetailFragment : Fragment() {

    private var _binding: FragmentAlbumDetailBinding? = null
    private val binding: FragmentAlbumDetailBinding get() = _binding!!

    private val args: AlbumDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (_binding == null) {
            _binding = FragmentAlbumDetailBinding.inflate(layoutInflater, container, false)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            activity?.onBackPressed()
        }
        binding.tvAlbumTitle.text = args.albumTitle
        TadpoleUtil.loadAvatar(context, binding.ivAlbumCover, args.albumCover)
        binding.tvAlbumDesc.text = args.albumDesc
        binding.tvAlbumSubscribe.text = "订阅量: ${args.albumSubscribeCount}"
        val tags = args.albumTags.split(",")
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
            AlbumInfoFragment.newInstance(args.albumIntro, args.albumRichInfo)
        } else {
            TrackListFragment.newInstance(args.albumId.toString(), args.totalCount, args.albumTitle)
        }

        override fun getPageTitle(position: Int): CharSequence? = titles[position]
    }

}