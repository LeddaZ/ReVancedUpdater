package it.leddaz.morpheupdater.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.noties.markwon.Markwon
import it.leddaz.morpheupdater.R
import it.leddaz.morpheupdater.utils.misc.CommonStuff.APP_REPO
import it.leddaz.morpheupdater.utils.misc.CommonStuff.openLink
import it.leddaz.morpheupdater.utils.plugins.CodeTypefacePlugin

/**
 * Dialog that shows when an app card is held.
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
            val codeTypeface = resources.getFont(R.font.gsanscode)
            val markwon = context?.let { it1 ->
                Markwon.builder(it1)
                    .usePlugin(CodeTypefacePlugin(codeTypeface))
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
                .setPositiveButton(R.string.ok) { _, _ -> dismiss() }
            if (fullButton) {
                builder.setNegativeButton(R.string.github) { _, _ ->
                    this.context?.let { it1 ->
                        openLink(
                            "https://github.com/$APP_REPO/releases",
                            it1
                        )
                    }
                }
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
