package it.leddaz.revancedupdater.utils.apputils

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.content.FileProvider
import it.leddaz.revancedupdater.BuildConfig
import java.io.File

/**
 * Installs an APK.
 * @author Leonardo Ledda (LeddaZ)
 */
class AppInstaller() {

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    constructor(fileName: String, context: Context) : this() {
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                var destination = context.getExternalFilesDir("/apks/").toString() + "/"
                destination += fileName
                val contentUri = FileProvider.getUriForFile(
                    context,
                    BuildConfig.APPLICATION_ID + ".provider",
                    File(destination)
                )
                val install = Intent(Intent.ACTION_VIEW)
                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                install.data = contentUri
                context.startActivity(install)
                context.unregisterReceiver(this)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_EXPORTED
            )
        } else {
            context.registerReceiver(
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }
    }

}
