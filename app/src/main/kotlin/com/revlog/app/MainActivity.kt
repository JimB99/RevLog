package com.revlog.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.revlog.app.ui.RevLogNavHost
import com.revlog.app.ui.theme.RevLogTheme
import com.revlog.app.ui.viewmodel.ImportViewModel
import com.revlog.app.util.LocaleController
import com.revlog.data.locale.LocalePreferences
import com.revlog.data.repository.AppSettings
import com.revlog.data.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository

    private val importViewModel: ImportViewModel by viewModels()
    private var startImportReview by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleImportIntent(intent)

        setContent {
            val context = LocalContext.current
            val settings by settingsRepository.settings.collectAsState(
                initial = remember {
                    AppSettings(languageTag = LocalePreferences.read(context))
                },
            )
            LaunchedEffect(settings.languageTag) {
                LocaleController.apply(settings.languageTag)
            }

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
