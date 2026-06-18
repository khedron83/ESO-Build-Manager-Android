package com.cubicserenity.esobuildmanager.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubicserenity.esobuildmanager.data.repository.BuildRepository
import com.cubicserenity.esobuildmanager.domain.model.Build
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BuildDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: BuildRepository,
) : ViewModel() {

    private val buildId: Long = savedStateHandle["buildId"] ?: 0L

    private val _build = MutableStateFlow<Build?>(null)
    val build: StateFlow<Build?> = _build.asStateFlow()

    init {
        viewModelScope.launch {
            _build.value = repo.getBuild(buildId)
        }
    }
}
