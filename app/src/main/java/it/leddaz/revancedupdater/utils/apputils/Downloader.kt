package it.leddaz.revancedupdater.utils.apputils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import it.leddaz.revancedupdater.LOG_TAG
import it.leddaz.revancedupdater.R
import java.io.File

/**
 * Downloads a file.
 * @author Leonardo Ledda (LeddaZ)
 */
class Downloader() {

    constructor(dlm: DownloadManager, context: Context, uri: Uri, fileName: String) : this() {
        downloadFile(dlm, context, uri, fileName)
    }

    private fun downloadFile(dlm: DownloadManager, context: Context, uri: Uri, fileName: String): Long {
        var downloadReference: Long = 0
        try {
            val request = DownloadManager.Request(uri)

            // Set title of request
            request.setTitle(fileName)

            // Set notification when download completed
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

            // Set the local destination for the downloaded file to a path within the application's external files directory
            var destination = context.getExternalFilesDir("/apks/").toString() + "/"
            destination += fileName
            request.setDestinationInExternalFilesDir(context, "/apks/", fileName)

            // Delete the APK before downloading if it already exists
            val apkFile = File(destination)
            if(apkFile.exists()) {
                apkFile.delete()
                Log.i(LOG_TAG, "Existing APK deleted.")
            }

            //Enqueue download and save the referenceId
            downloadReference = dlm.enqueue(request)
            Toast.makeText(context, R.string.download_started, Toast.LENGTH_LONG).show()
        } catch (e: IllegalArgumentException) {
            Toast.makeText(context, R.string.download_error, Toast.LENGTH_LONG).show()
            Log.e(LOG_TAG, e.printStackTrace().toString())
        }
        return downloadReference
    }

}
