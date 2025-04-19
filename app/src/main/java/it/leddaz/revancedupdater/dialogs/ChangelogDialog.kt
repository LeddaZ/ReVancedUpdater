package it.leddaz.revancedupdater.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.noties.markwon.Markwon
import it.leddaz.revancedupdater.R
import it.leddaz.revancedupdater.utils.misc.CommonStuff.openLink

/**
 * Dialog that shows when the About button is pressed.
 * @author Leonardo Ledda (LeddaZ)
 */
class ChangelogDialog(
    private val changelog: String,
    private val fullButton: Boolean,
    private val version: String = ""
) : DialogFragment() {
    /**
     * Actions executed when the dialog is created at runtime.
     * @property savedInstanceState
     * @return The AlertDialog.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        this.activity?.let { DynamicColors.applyToActivityIfAvailable(it) }
        return activity?.let {
            val markwon = context?.let { it1 ->
                Markwon.builder(it1)
                    .build()
            }
            var md = ""
            if (fullButton) {
                md += "# $version\n"
            }
            md += changelog
            val parsedMd = markwon?.toMarkdown(md)
            val builder = MaterialAlertDialogBuilder(it)
            builder.setMessage(parsedMd)
                .setNeutralButton(R.string.ok) { _, _ -> dismiss() }
            if (fullButton) {
                builder.setPositiveButton(R.string.github) { _, _ ->
                    this.context?.let { it1 ->
                        openLink(
                            "https://github.com/LeddaZ/ReVancedUpdater",
                            it1
                        )
                    }
                }
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
