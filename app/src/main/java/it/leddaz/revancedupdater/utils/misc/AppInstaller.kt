package it.leddaz.revancedupdater.utils.misc

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat.getString
import androidx.core.content.FileProvider
import com.google.android.material.progressindicator.LinearProgressIndicator
import it.leddaz.revancedupdater.BuildConfig
import it.leddaz.revancedupdater.R
import it.leddaz.revancedupdater.utils.misc.CommonStuff.LOG_TAG
import it.leddaz.revancedupdater.utils.misc.CommonStuff.requestInstallPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

/**
 * Downloads a file.
 * @author Leonardo Ledda (LeddaZ)
 */
class AppInstaller() {

    constructor(
        context: Context,
        uri: String,
        fileName: String,
        progressIndicator: LinearProgressIndicator,
        button: Button
    ) : this() {
        downloadFile(context, uri, fileName, progressIndicator, button)
    }

    private fun downloadFile(
        context: Context,
        url: String,
        fileName: String,
        progressIndicator: LinearProgressIndicator,
        button: Button
    ) {
        try {
            progressIndicator.visibility = View.VISIBLE
            button.isEnabled = false
            button.text = getString(context, R.string.downloading)

            // Delete the APK before downloading if it already exists
            val apkFile = File(context.getExternalFilesDir("/apks/").toString(), fileName)
            if (apkFile.exists()) {
                apkFile.delete()
            }

            CoroutineScope(Dispatchers.IO).launch {
                val connection = URL(url).openConnection()
                val fileLength = connection.contentLength
                val input = connection.getInputStream()
                val output =
                    File(context.getExternalFilesDir("/apks/").toString(), fileName).outputStream()
                val data = ByteArray(1024)
                var total: Long = 0
                var count: Int

                while (input.read(data).also { count = it } != -1) {
                    total += count.toLong()
                    val progress = (total * 100 / fileLength).toInt()

                    withContext(Dispatchers.Main) {
                        progressIndicator.setProgress(progress, true)
                        button.text = buildString {
                            append(getString(context, R.string.downloading))
                            append(" ")
                            append(progress)
                            append("%")
                        }
                    }

                    output.write(data, 0, count)
                }

                output.close()
                input.close()

                withContext(Dispatchers.Main) {
                    progressIndicator.visibility = View.GONE
                    button.text = getString(context, R.string.download_button_text)
                    button.isEnabled = true
                    installApk(fileName, context)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, R.string.download_error, Toast.LENGTH_LONG).show()
            Log.e(LOG_TAG, e.printStackTrace().toString())
        }
    }

    private fun installApk(fileName: String, context: Context) {
        requestInstallPermission(context)

        val apkUri: Uri =
            FileProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + ".provider",
                File(context.getExternalFilesDir("/apks/").toString(), fileName)
            )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    }

}
