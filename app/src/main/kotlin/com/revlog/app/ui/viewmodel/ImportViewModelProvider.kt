package com.revlog.app.ui.viewmodel

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun activityImportViewModel(): ImportViewModel {
    val activity = LocalContext.current as ComponentActivity
    return hiltViewModel(activity)
}
