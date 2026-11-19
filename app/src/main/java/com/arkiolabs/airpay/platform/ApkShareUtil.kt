package com.arkiolabs.airpay.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Helper for sharing Airpay's own APK via the system share sheet. The
 * approach mirrors what apps like ShareX/Bridgefy do — copy the installed
 * APK from the package's source path into the app's cache, expose it
 * through our FileProvider, and fire ACTION_SEND with `application/vnd.android.package-archive`.
 *
 * Falls back to a text-only share if the APK can't be read.
 */
object ApkShareUtil {

    private const val SHARE_BODY = "Try Airpay — 100% offline UPI payments without internet! Send money, check balance, and pay anywhere directly via SIM."
    private const val FALLBACK_BODY = "Try Airpay — 100% offline UPI payments without internet! Send money, check balance, and pay anywhere directly via SIM."

    /**
     * Shares the installed APK file. If the source APK can't be located
     * or copied, falls back to [shareLink] so the user always gets a
     * share sheet to send something useful.
     */
    fun shareInstalledApk(context: Context) {
        val sourceApk = locateSourceApk(context)
        if (sourceApk == null || !sourceApk.exists() || sourceApk.length() == 0L) {
            shareLink(context)
            return
        }

        val cacheCopy = File(context.cacheDir, "Airpay.apk")
        runCatching {
            sourceApk.copyTo(cacheCopy, overwrite = true)
        }.onFailure {
            shareLink(context)
            return
        }

        val authority = "${context.packageName}.fileprovider"
        val uri: Uri = runCatching {
            FileProvider.getUriForFile(context, authority, cacheCopy)
        }.getOrElse {
            shareLink(context)
            return
        }

        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.android.package-archive"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, SHARE_BODY)
            putExtra(Intent.EXTRA_SUBJECT, "Airpay")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        runCatching {
            context.startActivity(Intent.createChooser(send, "Share Airpay"))
        }.onFailure {
            shareLink(context)
        }
    }

    /**
     * Plain-text share fallback. Used when the APK can't be located or
     * the chooser fails.
     */
    fun shareLink(context: Context) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, FALLBACK_BODY)
            putExtra(Intent.EXTRA_SUBJECT, "Airpay")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            context.startActivity(Intent.createChooser(send, "Share Airpay"))
        }
    }

    /**
     * Shares a clean offline transaction receipt with counterparty and amount.
     */
    fun shareTransactionReceipt(
        context: Context,
        amount: String,
        payeeName: String,
        vpa: String,
        timestamp: Long
    ) {
        val date = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
        val body = "✓ Paid ₹$amount to $payeeName ($vpa) on $date via LocalPe Offline SIM Network. No internet needed!"
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, body)
            putExtra(Intent.EXTRA_SUBJECT, "LocalPe Payment Receipt")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            context.startActivity(Intent.createChooser(send, "Share Receipt"))
        }
    }

    /**
     * Tries to find the installed APK file on disk. Returns null when the
     * package was installed as a split-APK bundle (Play Store) — those
     * can't be re-shared as a single .apk.
     */
    private fun locateSourceApk(context: Context): File? {
        val info = runCatching {
            context.packageManager.getApplicationInfo(context.packageName, 0)
        }.getOrNull() ?: return null

        // Standalone APK install — sourceDir points to a single .apk file.
        val source = info.sourceDir ?: return null
        val file = File(source)
        if (file.isFile && file.canRead()) return file

        // Fall back to splitSourceDirs[0] if present
        val splits = info.splitSourceDirs
        if (!splits.isNullOrEmpty()) {
            val first = File(splits[0])
            if (first.isFile && first.canRead()) return first
        }
        return null
    }
}
