package com.revlog.app.share

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object RevLogShare {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun writeExportFile(context: Context, json: String, filename: String): File {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, filename)
        file.writeText(json)
        return file
    }

    fun buildFilename(vehicleNames: List<String>): String {
        val date = LocalDate.now().format(dateFormatter)
        return if (vehicleNames.size == 1) {
            val slug = vehicleNames.first()
                .lowercase()
                .replace(Regex("[^a-z0-9]+"), "-")
                .trim('-')
                .ifBlank { "fahrzeug" }
            "revlog-$date-$slug.revlog"
        } else {
            "revlog-alle-$date.revlog"
        }
    }

    fun shareFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.revlog+json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TITLE, file.name)
            setDataAndType(uri, "application/vnd.revlog+json")
            clipData = ClipData.newRawUri(file.name, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, null))
    }
}
