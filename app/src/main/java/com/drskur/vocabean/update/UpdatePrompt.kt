package com.drskur.vocabean.update

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class UpdateState(
    val update: AppUpdate? = null,
    val downloading: Boolean = false,
    val apk: File? = null,
    val error: String? = null
)

class UpdateViewModel(application: Application) : AndroidViewModel(application) {
    private val updater = AppUpdater(application)
    private val mutableState = MutableStateFlow(UpdateState())
    val state = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                mutableState.value = UpdateState(update = updater.check())
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Offline/first release: don't interrupt normal app use.
            }
        }
    }

    fun dismiss() { if (!state.value.downloading) mutableState.value = UpdateState() }

    fun download() {
        val update = state.value.update ?: return
        if (state.value.downloading) return
        mutableState.value = state.value.copy(downloading = true, error = null)
        viewModelScope.launch {
            try {
                val apk = updater.download(update)
                mutableState.value = state.value.copy(downloading = false, apk = apk)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mutableState.value = state.value.copy(downloading = false, error = e.message ?: "다운로드에 실패했습니다.")
            }
        }
    }

    fun install() {
        val file = state.value.apk ?: return
        try {
            updater.install(file)
            dismiss()
        } catch (e: Exception) {
            mutableState.value = state.value.copy(error = e.message ?: "설치 화면을 열 수 없습니다.")
        }
    }

    fun permissionError() {
        mutableState.value = state.value.copy(error = "설정에서 이 앱의 ‘알 수 없는 앱 설치’를 허용한 후 다시 시도하세요.")
    }
}

@Composable
fun UpdatePrompt(model: UpdateViewModel = viewModel()) {
    val state by model.state.collectAsState()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (context.packageManager.canRequestPackageInstalls()) model.install() else model.permissionError()
    }
    fun installOrRequestPermission() {
        if (context.packageManager.canRequestPackageInstalls()) {
            model.install()
        } else {
            try {
                permissionLauncher.launch(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}")))
            } catch (_: Exception) {
                model.permissionError()
            }
        }
    }
    LaunchedEffect(state.apk) {
        if (state.apk != null) installOrRequestPermission()
    }
    val update = state.update ?: return
    AlertDialog(
        onDismissRequest = model::dismiss,
        title = { Text("새 버전 ${update.versionName}") },
        text = {
            if (state.downloading) CircularProgressIndicator()
            else Text(state.error ?: update.notes.ifBlank { "새로운 업데이트를 설치할 수 있습니다." })
        },
        confirmButton = {
            TextButton(
                enabled = !state.downloading,
                onClick = { if (state.apk != null) installOrRequestPermission() else model.download() }
            ) { Text(if (state.apk != null) "설치" else "업데이트") }
        },
        dismissButton = {
            TextButton(enabled = !state.downloading, onClick = model::dismiss) { Text("나중에") }
        }
    )
}
