package com.revlog.app.import

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.IntentCompat
import com.revlog.data.backup.RevLogBackupManager

object RevLogImport {
    private val externalMimeTypes = setOf(
        "application/vnd.revlog+json",
        "application/json",
        "application/octet-stream",
        "text/plain",
    )

    fun readJsonFromIntent(context: Context, intent: Intent): String? {
        val uri = resolveUri(intent) ?: return null
        val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: return null
        val accepted = isRevlogSource(context, uri, intent) ||
            (isExternalImportIntent(intent) && RevLogBackupManager.looksLikeBackup(json))
        return json.takeIf { accepted }
    }

    private fun resolveUri(intent: Intent): Uri? = when (intent.action) {
        Intent.ACTION_VIEW -> intent.data
        Intent.ACTION_SEND -> IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
        else -> null
    }

    private fun isExternalImportIntent(intent: Intent): Boolean =
        intent.action == Intent.ACTION_VIEW || intent.action == Intent.ACTION_SEND

    fun isRevlogSource(context: Context, uri: Uri, intent: Intent? = null): Boolean {
        if (hasRevlogName(uri, context, intent)) {
            return true
        }
        val mimeType = intent?.type ?: context.contentResolver.getType(uri)
        return mimeType == "application/vnd.revlog+json"
    }

    private fun hasRevlogName(uri: Uri, context: Context, intent: Intent?): Boolean {
        val displayName = queryDisplayName(context, uri)
        if (displayName?.contains(".revlog", ignoreCase = true) == true) {
            return true
        }
        if (uri.scheme == "file" && uri.path?.endsWith(".revlog", ignoreCase = true) == true) {
            return true
        }
        val segment = uri.lastPathSegment
        if (segment?.contains(".revlog", ignoreCase = true) == true) {
            return true
        }
        val title = intent?.getStringExtra(Intent.EXTRA_TITLE)
        return title?.contains(".revlog", ignoreCase = true) == true
    }

    fun acceptsExternalMimeType(mimeType: String?): Boolean =
        mimeType == null || mimeType == "*/*" || mimeType in externalMimeTypes

    private fun queryDisplayName(context: Context, uri: Uri): String? {
        if (uri.scheme != "content") return uri.lastPathSegment
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index == -1 || !cursor.moveToFirst()) return@use null
            cursor.getString(index)
        }
    }
}
