package xh.zero.tadpolestory.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import timber.log.Timber
import android.app.Activity
import android.content.Context

import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import java.lang.Exception


abstract class BaseFragment<VIEW> : Fragment() {

    private var _binding: VIEW? = null
    protected val binding get() = _binding!!
    private var isFirstInit = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (_binding == null) {
            isFirstInit = true
            _binding = onCreateBindLayout(inflater, container, savedInstanceState)
        }
        return rootView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isFirstInit) return
        isFirstInit = false
        onFirstViewCreated(view, savedInstanceState)
    }

    abstract fun onCreateBindLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): VIEW

    abstract fun rootView() : View

    abstract fun onFirstViewCreated(view: View, savedInstanceState: Bundle?)

    fun back() {
        hideKeyboard()
        activity?.onBackPressed()
    }

    protected fun hideKeyboard() {
        val context = requireActivity()
        try {
            (context as Activity).window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            if ((context as Activity).currentFocus != null && (context as Activity).currentFocus!!
                    .windowToken != null
            ) {
                (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                    (context as Activity).currentFocus!!.windowToken, 0
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}