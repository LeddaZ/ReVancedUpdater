package it.leddaz.revancedupdater.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import it.leddaz.revancedupdater.R
import it.leddaz.revancedupdater.utils.misc.Utils.openLink

/**
 * Dialog that shows when the About button is pressed.
 * @author Leonardo Ledda (LeddaZ)
 */
class AboutDialog : DialogFragment() {
    /**
     * Actions executed when the dialog is created at runtime.
     * @property savedInstanceState
     * @return The AlertDialog.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        this.activity?.let { DynamicColors.applyToActivityIfAvailable(it) }
        return activity?.let {
            val builder = MaterialAlertDialogBuilder(it)
            builder.setTitle(R.string.about_dialog_title)
            builder.setMessage(R.string.about_dialog_text)
                .setNeutralButton(R.string.ok) { _, _ -> dismiss() }
                .setPositiveButton(R.string.open_github) { _, _ ->
                    this.context?.let { it1 ->
                        openLink(
                            "https://github.com/LeddaZ/ReVancedUpdater",
                            it1
                        )
                    }
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
