package com.dokodemo.ui.screens.subscription

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokodemo.R
import com.dokodemo.core.SubscriptionFetcher
import com.dokodemo.core.SubscriptionSyncManager
import com.dokodemo.data.model.Subscription
import com.dokodemo.data.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubscriptionUiState(
    val subscriptions: List<Subscription> = emptyList(),
    val isRefreshing: Boolean = false,
    val refreshingId: Long? = null,
    val successCount: Int? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val subscriptionRepository: SubscriptionRepository,
    private val subscriptionSyncManager: SubscriptionSyncManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            subscriptionRepository.getAllSubscriptions().collect { subs -> _uiState.update { it.copy(subscriptions = subs) } }
        }
    }

    fun addSubscription(name: String, url: String) {
        if (!validate(name, url)) return
        refreshOperation {
            val sub = Subscription(name = name.trim(), url = url.trim())
            val id = subscriptionRepository.insertSubscription(sub)
            refresh(sub.copy(id = id))
        }
    }

    fun deleteSubscription(subscription: Subscription) = mutate {
        subscriptionRepository.deleteSubscription(subscription)
    }

    fun editSubscription(subscription: Subscription, newName: String, newUrl: String) {
        if (!validate(newName, newUrl)) return
        mutate { subscriptionRepository.updateSubscription(subscription.copy(name = newName.trim(), url = newUrl.trim())) }
    }

    fun updateAllSubscriptions() = refreshOperation {
        var total = 0
        var succeeded = false
        for (sub in _uiState.value.subscriptions.filter { it.isActive }) {
            val count = refresh(sub)
            if (count != null) { total += count; succeeded = true }
        }
        if (succeeded) _uiState.update { it.copy(successCount = total) }
    }

    fun updateSubscription(subscription: Subscription) = refreshOperation { refresh(subscription) }

    private fun refreshOperation(operation: suspend () -> Unit) {
        if (_uiState.value.isRefreshing) return
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null, successCount = null) }
        viewModelScope.launch {
            try { operation() }
            catch (e: CancellationException) { throw e } catch (_: Exception) {
                _uiState.update { it.copy(errorMessage = context.getString(R.string.save_failed)) }
            } finally { _uiState.update { it.copy(isRefreshing = false, refreshingId = null) } }
        }
    }

    private fun mutate(operation: suspend () -> Unit) {
        viewModelScope.launch {
            try { operation() }
            catch (e: CancellationException) { throw e } catch (_: Exception) {
                _uiState.update { it.copy(errorMessage = context.getString(R.string.save_failed)) }
            }
        }
    }

    private suspend fun refresh(sub: Subscription): Int? {
        _uiState.update { it.copy(refreshingId = sub.id) }
        return subscriptionSyncManager.refresh(sub.id).fold(
            onSuccess = { count -> _uiState.update { it.copy(successCount = count) }; count },
            onFailure = { error ->
                val message = if (error is SubscriptionFetcher.InvalidContentException) context.getString(R.string.subscription_formats)
                    else context.getString(R.string.subscription_update_failed, sub.name)
                _uiState.update { it.copy(errorMessage = message) }
                null
            }
        )
    }

    private fun validate(name: String, url: String): Boolean {
        val valid = name.isNotBlank() && runCatching {
            val uri = java.net.URI(url.trim())
            uri.scheme in listOf("https", "http") && !uri.host.isNullOrBlank()
        }.getOrDefault(false)
        if (!valid) _uiState.update { it.copy(errorMessage = context.getString(R.string.review_sub_invalid)) }
        return valid
    }

    fun clearSuccess() { _uiState.update { it.copy(successCount = null) } }
    fun clearError() { _uiState.update { it.copy(errorMessage = null) } }
}
