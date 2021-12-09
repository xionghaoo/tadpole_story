package xh.zero.tadpolestory.utils

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import xh.zero.tadpolestory.R

typealias ViewConfigCallback = (v: View, requestDismiss: () -> Unit) -> Unit
typealias OperationCallback = (v: View) -> Unit
typealias OnDismissListener = (v: View) -> Unit

class PromptDialog private constructor(
    private val context: Context,
    private val theme: Int,
    @LayoutRes
    private val layoutId: Int,
    private val isCancelable: Boolean,
    private val isTransparent: Boolean = false,
    private var callback: ViewConfigCallback?,
    private var operations: ArrayList<Operation> = ArrayList(),
    private val isRemoveTransparentBg: Boolean,
    private val onDismiss: OnDismissListener?
){

    private var dialog: AlertDialog

    init {
        val contentView = LayoutInflater.from(context).inflate(layoutId, null)
        dialog = AlertDialog.Builder(context, theme)
            .setCancelable(isCancelable)
            .setView(contentView)
            .create()
        callback?.invoke(contentView) { dialog.dismiss() }
        if (isTransparent) {
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        if (isRemoveTransparentBg) {
            contentView.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.setOnDismissListener {
            onDismiss?.invoke(contentView)
        }

        operations.forEach { operation ->
            when (operation.type) {
                OperationType.CONFIRM, OperationType.CANCEL -> {
                    contentView.findViewById<View>(operation.viewId).setOnClickListener {
                        operation.operation?.invoke(contentView)
                        if (operation.autoDismiss) {
                            dialog.dismiss()
                        }
                    }
                }
            }
        }
    }

    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }

    class Builder(
        private val context: Context,
        private val theme: Int = 0
    ) {
        private var isTransparent: Boolean = false
        private var layoutId: Int? = null
        private var callback: ViewConfigCallback? = null
        private var operations: ArrayList<Operation> = ArrayList()
        private var isRemoveTransparentBg: Boolean = false
        private var onDismiss: OnDismissListener? = null
        private var isCancelable: Boolean = true

        fun setViewId(@LayoutRes layoutId: Int) : Builder {
            this.layoutId = layoutId
            return this
        }

        fun isTransparent(isTransparent: Boolean) : Builder {
            this.isTransparent = isTransparent
            return this
        }

        fun removeRedundantArea() : Builder {
            isRemoveTransparentBg = true
            return this
        }

        fun addOperation(
            type: OperationType,
            viewId: Int,
            autoDismiss: Boolean = true,
            operation: OperationCallback?
        ) : Builder {
            operations.add(Operation(type, viewId, autoDismiss, operation))
            return this
        }

        fun addDismissListener(listener: OnDismissListener) : Builder {
            this.onDismiss = listener
            return this
        }

        fun configView(callback: ViewConfigCallback?) : Builder {
            this.callback = callback
            return this
        }

        fun setCancelable(cancelable: Boolean) : Builder {
            isCancelable = cancelable
            return this
        }

        fun build() : PromptDialog {
            if (layoutId == null) {
                throw IllegalStateException("layout id is null")
            }
            return PromptDialog(
                context = context,
                    theme = theme,
                layoutId = layoutId!!,
                isTransparent = isTransparent,
                isCancelable = isCancelable,
                callback = callback,
                operations = operations,
                isRemoveTransparentBg = isRemoveTransparentBg,
                onDismiss = onDismiss
            )
        }
    }

    companion object {
        fun showCommon(
            context: Context,
            title: String = "提示",
            message: String,
            isCancelable: Boolean = true,
            confirmTxt: String = "确认",
            onConfirm: () -> Unit
        ) {
            Builder(context)
                .setViewId(R.layout.dialog_common)
                .setCancelable(isCancelable)
                .isTransparent(true)
                .configView { v, requestDismiss ->
                    v.findViewById<TextView>(R.id.tv_dialog_title).text = title
                    v.findViewById<TextView>(R.id.tv_dialog_message).text = message
                    v.findViewById<TextView>(R.id.btn_confirm).text
                    v.findViewById<TextView>(R.id.btn_cancel).text
                    if (!isCancelable) {
                        v.findViewById<TextView>(R.id.btn_cancel).visibility = View.GONE
                        val lp = v.findViewById<TextView>(R.id.btn_confirm).layoutParams as FrameLayout.LayoutParams
                        lp.gravity = Gravity.CENTER
                    }
                }
                .addOperation(OperationType.CONFIRM, R.id.btn_confirm, true) {
                    onConfirm()
                }
                .addOperation(OperationType.CANCEL, R.id.btn_cancel, true) {}
                .build()
                .show()
        }
    }

}

enum class OperationType {
    CONFIRM, CANCEL
}

class Operation(
    val type: OperationType,
    val viewId: Int,
    val autoDismiss: Boolean = true,
    val operation: OperationCallback?
) {

    override fun hashCode(): Int {
        return type.hashCode()
    }

    override fun equals(other: Any?): Boolean =
        if (other is Operation) type == other.type else false
}