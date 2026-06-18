package com.cubicserenity.esobuildmanager.ui.builds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubicserenity.esobuildmanager.data.repository.BuildRepository
import com.cubicserenity.esobuildmanager.data.sync.SyncResult
import com.cubicserenity.esobuildmanager.data.sync.WebDavSyncClient
import com.cubicserenity.esobuildmanager.domain.model.Build
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BuildsUiState(
    val allBuilds: List<Build> = emptyList(),
    val query: String = "",
    val roleFilter: String = "",
    val isSyncing: Boolean = false,
    val syncResult: SyncResult? = null,
) {
    val filtered: List<Build>
        get() = allBuilds
            .filter { roleFilter.isBlank() || it.role == roleFilter }
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
}

@HiltViewModel
class BuildsViewModel @Inject constructor(
    private val repo: BuildRepository,
    private val syncClient: WebDavSyncClient,
) : ViewModel() {

    private val _extra = MutableStateFlow(BuildsUiState())

    val state: StateFlow<BuildsUiState> = combine(
        repo.builds,
        _extra,
    ) { builds, extra ->
        extra.copy(allBuilds = builds)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BuildsUiState())

    fun setQuery(q: String) = _extra.update { it.copy(query = q) }

    fun setRoleFilter(role: String) = _extra.update {
        it.copy(roleFilter = if (it.roleFilter == role) "" else role)
    }

    fun deleteBuild(id: Long) {
        viewModelScope.launch { repo.deleteBuild(id) }
    }

    fun sync() {
        viewModelScope.launch {
            _extra.update { it.copy(isSyncing = true, syncResult = null) }
            val result = syncClient.sync()
            _extra.update { it.copy(isSyncing = false, syncResult = result) }
        }
    }

    fun clearSyncResult() = _extra.update { it.copy(syncResult = null) }
}
