package com.cubicserenity.esobuildmanager.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cubicserenity.esobuildmanager.domain.model.Build
import com.cubicserenity.esobuildmanager.domain.model.GearPiece
import com.cubicserenity.esobuildmanager.util.GEAR_SLOTS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildDetailScreen(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    viewModel: BuildDetailViewModel = hiltViewModel(),
) {
    val build by viewModel.build.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(build?.name ?: "Build") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                actions = {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit") }
                },
            )
        },
    ) { padding ->
        if (build == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        BuildSheet(build = build!!, modifier = Modifier.padding(padding))
    }
}

@Composable
fun BuildSheet(build: Build, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header badges
        val tags = buildList {
            if (build.esoClass.isNotBlank()) add(build.esoClass)
            if (build.subclass1.isNotBlank()) {
                val sub = buildString {
                    append(build.subclass1)
                    if (build.subclass2.isNotBlank()) append(" / ${build.subclass2}")
                }
                add("($sub)")
            }
            if (build.role.isNotBlank()) add(build.role)
            if (build.content.isNotBlank()) add(build.content)
            if (build.gamePatch.isNotBlank()) add(build.gamePatch)
        }
        if (tags.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                tags.forEach { tag ->
                    AssistChip(onClick = {}, label = { Text(tag, style = MaterialTheme.typography.labelSmall) })
                }
            }
        }
        if (build.source.isNotBlank()) {
            Text(
                "Source: ${build.source}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        HorizontalDivider()

        // Skills
        SectionHeader("Skills")
        for ((barIdx, barLabel) in listOf(0 to "Front Bar", 1 to "Back Bar")) {
            Text(barLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            val barSkills = build.skills.filter { it.bar == barIdx }.associateBy { it.slot }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (slot in 0..4) {
                    SkillChip(barSkills[slot]?.name ?: "—", Modifier.weight(1f))
                }
            }
            val ult = barSkills[5]?.name ?: "—"
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ult:", style = MaterialTheme.typography.labelMedium)
                SkillChip(ult, Modifier.fillMaxWidth())
            }
            if (barIdx == 0) Spacer(Modifier.height(4.dp))
        }

        // Champion Points
        val cpSlots = build.cpSlots
        if (cpSlots.any { it.isNotBlank() }) {
            HorizontalDivider()
            SectionHeader("Champion Points")
            if (build.championPoints.isNotBlank()) {
                Text(build.championPoints, style = MaterialTheme.typography.bodySmall)
            }
            listOf("Craft" to 0, "Warfare" to 4, "Fitness" to 8).forEach { (tree, start) ->
                Text(tree, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (i in start until start + 4) {
                        SkillChip(cpSlots.getOrElse(i) { "" }.ifBlank { "—" }, Modifier.weight(1f))
                    }
                }
            }
        }

        HorizontalDivider()

        // Gear
        SectionHeader("Gear")
        val gearBySlot = build.gear.associateBy { it.slot }
        GEAR_SLOTS.forEach { slot ->
            val piece = gearBySlot[slot]
            GearRow(slot, piece)
        }

        HorizontalDivider()

        // Stats
        SectionHeader("Stats & Buffs")
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatPill("Health", build.attributeHealth)
            StatPill("Magicka", build.attributeMagicka)
            StatPill("Stamina", build.attributeStamina)
        }
        if (build.foodBuff.isNotBlank()) {
            LabeledValue("Food", build.foodBuff)
        }
        if (build.mundusStone.isNotBlank()) {
            LabeledValue("Mundus", build.mundusStone)
        }

        // Notes
        if (build.notes.isNotBlank()) {
            HorizontalDivider()
            SectionHeader("Notes")
            Text(build.notes, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun SkillChip(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
        )
    }
}

@Composable
private fun GearRow(slot: String, piece: GearPiece?) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(slot, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(90.dp), color = MaterialTheme.colorScheme.secondary)
        if (piece?.weight == "N/A") {
            Text("N/A (two-handed)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        } else {
            val parts = buildList {
                piece?.setName?.takeIf { it.isNotBlank() }?.let { add(it) }
                piece?.weight?.takeIf { it.isNotBlank() && it != "—" }?.let { add(it) }
                piece?.trait?.takeIf { it.isNotBlank() }?.let { add(it) }
                piece?.quality?.takeIf { it.isNotBlank() }?.let { add(it) }
            }
            Text(
                if (parts.isEmpty()) "—" else parts.joinToString("  ·  "),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun StatPill(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun LabeledValue(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("$label:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.width(56.dp))
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}
