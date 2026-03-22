package com.dokodemo.ui.screens.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokodemo.core.SubscriptionFetcher
import com.dokodemo.data.model.Group
import com.dokodemo.data.model.Subscription
import com.dokodemo.data.repository.GroupRepository
import com.dokodemo.data.repository.ServerRepository
import com.dokodemo.data.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubscriptionUiState(
    val subscriptions: List<Subscription> = emptyList(),
    val isRefreshing: Boolean = false,
    val refreshingId: Long? = null, // 当前正在刷新的订阅ID
    val errorMessage: String? = null
)

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val groupRepository: GroupRepository,
    private val subscriptionFetcher: SubscriptionFetcher,
    private val serverRepository: ServerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            subscriptionRepository.getAllSubscriptions().collect { subs ->
                _uiState.update { it.copy(subscriptions = subs) }
            }
        }
    }

    fun addSubscription(name: String, url: String) {
        if (name.isBlank() || url.isBlank()) {
            _uiState.update { it.copy(errorMessage = "名称或链接不能为空") }
            return
        }

        viewModelScope.launch {
            val sub = Subscription(name = name, url = url)
            subscriptionRepository.insertSubscription(sub)
        }
    }

    fun deleteSubscription(subscription: Subscription) {
        viewModelScope.launch {
            subscriptionRepository.deleteSubscription(subscription)
            val group = groupRepository.getGroupBySubscriptionId(subscription.id)
            if (group != null) {
                groupRepository.updateGroup(group.copy(subscriptionId = null))
            }
        }
    }

    fun editSubscription(subscription: Subscription, newName: String, newUrl: String) {
        if (newName.isBlank() || newUrl.isBlank()) {
            _uiState.update { it.copy(errorMessage = "名称或链接不能为空") }
            return
        }
        viewModelScope.launch {
            subscriptionRepository.updateSubscription(subscription.copy(name = newName, url = newUrl))
        }
    }

    fun updateAllSubscriptions() {
        if (_uiState.value.isRefreshing) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            
            val subs = _uiState.value.subscriptions
            for (sub in subs) {
                if (sub.isActive) {
                    refreshSingleSubscription(sub)
                }
            }
            
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun updateSubscription(subscription: Subscription) {
        viewModelScope.launch {
            _uiState.update { it.copy(refreshingId = subscription.id, errorMessage = null) }
            refreshSingleSubscription(subscription)
            _uiState.update { it.copy(refreshingId = null) }
        }
    }

    private suspend fun refreshSingleSubscription(sub: Subscription) {
        var group = groupRepository.getGroupBySubscriptionId(sub.id)
        if (group == null) {
            val newGroupId = groupRepository.insertGroup(Group(name = sub.name, subscriptionId = sub.id))
            group = groupRepository.getGroupById(newGroupId)
        }
        
        val result = subscriptionFetcher.fetchAndParse(sub.url, group?.id)
        result.onSuccess { nodes ->
            serverRepository.replaceServersForSubscription(sub.id, nodes)
            subscriptionRepository.updateSyncStatus(sub.id, System.currentTimeMillis(), nodes.size)
        }.onFailure { e ->
            _uiState.update { it.copy(errorMessage = "更新失败 ${sub.name}: ${e.message}") }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
