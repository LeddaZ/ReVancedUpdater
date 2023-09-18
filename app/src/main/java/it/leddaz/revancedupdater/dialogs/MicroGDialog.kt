package it.leddaz.revancedupdater.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import it.leddaz.revancedupdater.R
import it.leddaz.revancedupdater.microGDownloadUrl
import it.leddaz.revancedupdater.utils.misc.Utils.dlAndInstall


/**
 * Dialog that shows when Vanced microG isn't installed.
 * @author Leonardo Ledda (LeddaZ)
 */
class MicroGDialog : DialogFragment() {
    /**
     * Actions executed when the dialog is created at runtime.
     * @property savedInstanceState
     * @return The AlertDialog.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        this.activity?.let { DynamicColors.applyToActivityIfAvailable(it) }
        return activity?.let {
            val builder = MaterialAlertDialogBuilder(it)
            builder.setTitle(R.string.microg_dialog_title)
            builder.setMessage(R.string.microg_dialog_text)
                .setPositiveButton(R.string.yes) { _, _ ->
                    this.context?.let { it1 ->
                        dlAndInstall(
                            "vanced-microG.apk", microGDownloadUrl,
                            it1
                        )
                    }
                }
                .setNegativeButton(R.string.no) { _, _ -> dismiss() }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
