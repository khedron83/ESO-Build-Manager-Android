package com.cubicserenity.esobuildmanager.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cubicserenity.esobuildmanager.data.repository.BuildRepository
import com.cubicserenity.esobuildmanager.domain.model.Build
import com.cubicserenity.esobuildmanager.domain.model.GearPiece
import com.cubicserenity.esobuildmanager.domain.model.Skill
import com.cubicserenity.esobuildmanager.util.GEAR_SLOTS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BuildEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: BuildRepository,
) : ViewModel() {

    private val buildId: Long = savedStateHandle["buildId"] ?: 0L
    private val isNew: Boolean = buildId == 0L

    private val _build = MutableStateFlow(emptyDraft())
    val build: StateFlow<Build> = _build.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    init {
        if (!isNew) {
            viewModelScope.launch {
                repo.getBuild(buildId)?.let { _build.value = it }
            }
        }
    }

    fun update(block: Build.() -> Build) = _build.update { it.block() }

    fun setSkill(bar: Int, slot: Int, name: String) = _build.update { b ->
        val skills = b.skills.toMutableList()
        val idx = skills.indexOfFirst { it.bar == bar && it.slot == slot }
        if (idx >= 0) skills[idx] = skills[idx].copy(name = name)
        else skills.add(Skill(buildId = buildId, bar = bar, slot = slot, name = name))
        b.copy(skills = skills)
    }

    fun setGear(slot: String, updated: GearPiece) = _build.update { b ->
        val gear = b.gear.toMutableList()
        val idx = gear.indexOfFirst { it.slot == slot }
        if (idx >= 0) gear[idx] = updated else gear.add(updated)
        b.copy(gear = gear)
    }

    fun setCpSlot(index: Int, value: String) = _build.update { b ->
        val slots = b.cpSlots.toMutableList()
        while (slots.size <= index) slots.add("")
        slots[index] = value
        b.copy(cpSlots = slots)
    }

    fun save() {
        viewModelScope.launch {
            repo.saveBuild(_build.value)
            _isSaved.value = true
        }
    }

    private fun emptyDraft() = Build(
        gear = GEAR_SLOTS.map { GearPiece(slot = it) },
        skills = (0..1).flatMap { bar -> (0..5).map { slot -> Skill(bar = bar, slot = slot) } },
    )
}
