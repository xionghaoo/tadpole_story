package xh.zero.tadpolestory.ui.more

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import xh.zero.tadpolestory.BuildConfig
import xh.zero.tadpolestory.R
import xh.zero.tadpolestory.databinding.FragmentMoreUpdateBinding
import xh.zero.tadpolestory.ui.BaseFragment
import xh.zero.tadpolestory.utils.OperationType
import xh.zero.tadpolestory.utils.PromptDialog

class MoreUpdateFragment : BaseFragment<FragmentMoreUpdateBinding>() {

    override fun onCreateBindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentMoreUpdateBinding {
        return FragmentMoreUpdateBinding.inflate(inflater, container, false)
    }

    override fun rootView(): View = binding.root

    override fun onFirstViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvVersionName.text = "当前版本：${BuildConfig.VERSION_NAME}"
        binding.tvUpdateTime.text = "更新时间：-"
        binding.containerUpdate.setOnClickListener {
            PromptDialog.Builder(requireContext())
                .isTransparent(true)
                .setViewId(R.layout.dialog_version_update)
                .configView { v, requestDismiss ->
                    val tvMessage = v.findViewById<TextView>(R.id.tv_dialog_message)
                    tvMessage.text = "版本号：V1.0.0\n版本特性：xxx"
                }
                .addOperation(OperationType.CANCEL, R.id.btn_cancel, true, null)
                .build()
                .show()
        }
    }

    companion object {
        fun newInstance() = MoreUpdateFragment()
    }
}