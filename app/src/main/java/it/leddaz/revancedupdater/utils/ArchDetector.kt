package it.leddaz.revancedupdater.utils

import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Reads the "ro.product.cpu.abilist" property to detect the device's
 * OS architecture.
 * @author Leonardo Ledda (LeddaZ)
 */
object ArchDetector {

    private const val LOG_TAG = "ReVanced Updater"

    /**
     * Reads the property.
     * @return The ABIs supported by the device
     */
    private fun readABIList(): String {
        var process: Process? = null
        var bufferedReader: BufferedReader? = null

        return try {
            process = ProcessBuilder().command("/system/bin/getprop", "ro.product.cpu.abilist")
                .redirectErrorStream(true).start()
            bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            var line = bufferedReader.readLine()
            if (line == null)
                line = "" // Prop not set
            line
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to read ro.product.cpu.abilist", e)
            ""
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close()
                } catch (_: IOException) {
                }
            }
            process?.destroy()
        }
    }

    /**
     * Returns the device architecture.
     * @return Device architecture
     */
    fun getArch(): String {
        val abilist: String = readABIList()
        Log.i(LOG_TAG, "Supported ABIs: $abilist")
        if (abilist.contains("x86_64"))
            return "x86_64"
        else if (abilist.contains("x86"))
            return "x86"
        else if (abilist.contains("arm64-v8a"))
            return "arm64"
        else if (abilist.contains("armeabi-v7a"))
            return "arm"
        else {
            Log.e(LOG_TAG, "Unsupported architecture! (goofy ahh device)")
            return ""
        }
    }

}
