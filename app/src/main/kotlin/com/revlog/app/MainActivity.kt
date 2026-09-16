package com.revlog.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.revlog.app.ui.RevLogNavHost
import com.revlog.app.ui.theme.RevLogTheme
import com.revlog.app.ui.viewmodel.ImportViewModel
import com.revlog.app.util.LocaleController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val importViewModel: ImportViewModel by viewModels()
    private var startImportReview by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        LocaleController.applyStored(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleImportIntent(intent)

        setContent {
            RevLogTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RevLogNavHost(startImportReview = startImportReview)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleImportIntent(intent)
    }

    private fun handleImportIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        if (intent.action != Intent.ACTION_VIEW) return
        val json = contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: return
        importViewModel.parseImport(json)
        startImportReview = true
    }
}
