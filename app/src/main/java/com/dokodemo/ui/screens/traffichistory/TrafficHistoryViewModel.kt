package com.dokodemo.ui.screens.traffichistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokodemo.data.dao.TrafficRecordDao
import com.dokodemo.data.model.TrafficRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrafficHistoryUiState(
    val records: List<TrafficRecord> = emptyList(),
    val totalUpload: Long = 0L,
    val totalDownload: Long = 0L
)

@HiltViewModel
class TrafficHistoryViewModel @Inject constructor(
    private val trafficRecordDao: TrafficRecordDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrafficHistoryUiState())
    val uiState: StateFlow<TrafficHistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            trafficRecordDao.getAllHistory().collect { records ->
                val totalUpload = records.sumOf { it.uploadBytes }
                val totalDownload = records.sumOf { it.downloadBytes }
                _uiState.value = TrafficHistoryUiState(
                    records = records,
                    totalUpload = totalUpload,
                    totalDownload = totalDownload
                )
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            trafficRecordDao.clearHistory()
        }
    }
}
