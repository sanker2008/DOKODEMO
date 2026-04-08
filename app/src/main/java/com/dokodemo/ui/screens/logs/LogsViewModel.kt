package com.dokodemo.ui.screens.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

@HiltViewModel
class LogsViewModel @Inject constructor() : ViewModel() {

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()
    
    private var lastLogCount = 0

    init {
        refreshLogs()
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (isActive) {
                delay(2000)
                refreshLogs()
            }
        }
    }

    fun refreshLogs() {
        viewModelScope.launch {
            val newLogs = withContext(Dispatchers.IO) {
                try {
                    val process = Runtime.getRuntime().exec("logcat -d -t 2000 -v time CoreManager:I DokoDemoVpnService:I *:S")
                    val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
                    val logList = mutableListOf<String>()
                    var line: String?
                    while (bufferedReader.readLine().also { line = it } != null) {
                        line?.let { logList.add(it) }
                    }
                    if (logList.isEmpty()) {
                        logList.add("No recent Xray core logs found.")
                    }
                    logList
                } catch (e: Exception) {
                    listOf("Error reading logs: ${e.message}")
                }
            }
            if (newLogs.size != lastLogCount || _logs.value.isEmpty()) {
                _logs.value = newLogs
                lastLogCount = newLogs.size
            }
        }
    }
    
    fun clearLogs() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    Runtime.getRuntime().exec("logcat -c")
                } catch (e: Exception) {
                }
            }
            _logs.value = emptyList()
            lastLogCount = 0
        }
    }
}
