package com.cubicserenity.esobuildmanager.ui.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cubicserenity.esobuildmanager.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildEditorScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: BuildEditorViewModel = hiltViewModel(),
) {
    val build by viewModel.build.collectAsStateWithLifecycle()
    val isSaved by viewModel.isSaved.collectAsStateWithLifecycle()

    LaunchedEffect(isSaved) {
        if (isSaved) onSaved()
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Info", "Skills", "Gear", "Stats", "Notes")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (build.id == 0L) "New Build" else "Edit Build") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.Close, "Cancel") }
                },
                actions = {
                    TextButton(onClick = viewModel::save) {
                        Icon(Icons.Default.Check, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Save")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 0.dp) {
                tabs.forEachIndexed { i, title ->
                    Tab(
                        selected = selectedTab == i,
                        onClick = { selectedTab = i },
                        text = { Text(title) },
                    )
                }
            }
            when (selectedTab) {
                0 -> InfoTab(build, viewModel)
                1 -> SkillsTab(build, viewModel)
                2 -> GearTab(build, viewModel)
                3 -> StatsTab(build, viewModel)
                4 -> NotesTab(build, viewModel)
            }
        }
    }
}

@Composable
private fun InfoTab(build: com.cubicserenity.esobuildmanager.domain.model.Build, vm: BuildEditorViewModel) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = build.name,
            onValueChange = { vm.update { copy(name = it) } },
            label = { Text("Build Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Shield, null) },
        )

        DropdownField("Class", build.esoClass, ESO_CLASSES) { vm.update { copy(esoClass = it) } }
        DropdownField("Subclass 1", build.subclass1, listOf("") + ESO_CLASSES) { vm.update { copy(subclass1 = it) } }
        DropdownField("Subclass 2", build.subclass2, listOf("") + ESO_CLASSES) { vm.update { copy(subclass2 = it) } }
        DropdownField("Role", build.role, ROLES) { vm.update { copy(role = it) } }
        DropdownField("Content", build.content, CONTENT_TYPES) { vm.update { copy(content = it) } }
        DropdownField("Patch", build.gamePatch, GAME_PATCHES) { vm.update { copy(gamePatch = it) } }

        OutlinedTextField(
            value = build.source,
            onValueChange = { vm.update { copy(source = it) } },
            label = { Text("Source URL / Creator") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Link, null) },
        )
    }
}

@Composable
private fun SkillsTab(build: com.cubicserenity.esobuildmanager.domain.model.Build, vm: BuildEditorViewModel) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        for ((barIdx, barLabel) in listOf(0 to "Front Bar", 1 to "Back Bar")) {
            Text(barLabel, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            val barSkills = build.skills.filter { it.bar == barIdx }.associateBy { it.slot }
            for (slot in 0..4) {
                OutlinedTextField(
                    value = barSkills[slot]?.name ?: "",
                    onValueChange = { vm.setSkill(barIdx, slot, it) },
                    label = { Text("Slot ${slot + 1}") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
            OutlinedTextField(
                value = barSkills[5]?.name ?: "",
                onValueChange = { vm.setSkill(barIdx, 5, it) },
                label = { Text("Ultimate") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
    }
}

@Composable
private fun GearTab(build: com.cubicserenity.esobuildmanager.domain.model.Build, vm: BuildEditorViewModel) {
    val gearBySlot = build.gear.associateBy { it.slot }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        GEAR_SLOTS.forEach { slot ->
            val piece = gearBySlot[slot] ?: com.cubicserenity.esobuildmanager.domain.model.GearPiece(slot = slot)
            GearSlotEditor(slot = slot, piece = piece, onUpdate = { updated -> vm.setGear(slot, updated) })
        }
    }
}

@Composable
private fun GearSlotEditor(
    slot: String,
    piece: com.cubicserenity.esobuildmanager.domain.model.GearPiece,
    onUpdate: (com.cubicserenity.esobuildmanager.domain.model.GearPiece) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val isOffHand = slot in OFF_HAND_SLOTS
    val isJewelry = slot in JEWELRY_SLOTS
    val isWeapon = slot in WEAPON_SLOTS

    Card {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(slot, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                if (piece.setName.isNotBlank()) {
                    Text(piece.setName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(4.dp))
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                }
            }
            if (expanded) {
                OutlinedTextField(
                    value = piece.setName,
                    onValueChange = { onUpdate(piece.copy(setName = it)) },
                    label = { Text("Set Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                if (!isJewelry) {
                    val weights = if (isOffHand) OFF_HAND_WEIGHTS else ARMOR_WEIGHTS
                    DropdownField("Weight", piece.weight, weights) { onUpdate(piece.copy(weight = it)) }
                }
                val traits = when {
                    isJewelry -> JEWELRY_TRAITS
                    isWeapon -> WEAPON_TRAITS
                    else -> ARMOR_TRAITS
                }
                DropdownField("Trait", piece.trait, traits) { onUpdate(piece.copy(trait = it)) }
                OutlinedTextField(
                    value = piece.enchant,
                    onValueChange = { onUpdate(piece.copy(enchant = it)) },
                    label = { Text("Enchant") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                DropdownField("Quality", piece.quality, QUALITY_TIERS) { onUpdate(piece.copy(quality = it)) }
            }
        }
    }
}

@Composable
private fun StatsTab(build: com.cubicserenity.esobuildmanager.domain.model.Build, vm: BuildEditorViewModel) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Attribute Points", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        IntField("Health", build.attributeHealth) { vm.update { copy(attributeHealth = it) } }
        IntField("Magicka", build.attributeMagicka) { vm.update { copy(attributeMagicka = it) } }
        IntField("Stamina", build.attributeStamina) { vm.update { copy(attributeStamina = it) } }

        HorizontalDivider()
        Text("Buffs", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

        DropdownField("Mundus Stone", build.mundusStone, listOf("") + MUNDUS_STONES) { vm.update { copy(mundusStone = it) } }

        OutlinedTextField(
            value = build.foodBuff,
            onValueChange = { vm.update { copy(foodBuff = it) } },
            label = { Text("Food / Drink Buff") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        HorizontalDivider()
        Text("Champion Points", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

        OutlinedTextField(
            value = build.championPoints,
            onValueChange = { vm.update { copy(championPoints = it) } },
            label = { Text("CP Notes") },
            modifier = Modifier.fillMaxWidth(),
        )

        CP_TREE_LABELS.forEachIndexed { treeIdx, tree ->
            Text(tree, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            val start = treeIdx * 4
            for (i in start until start + 4) {
                OutlinedTextField(
                    value = build.cpSlots.getOrElse(i) { "" },
                    onValueChange = { vm.setCpSlot(i, it) },
                    label = { Text("Star ${i - start + 1}") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
    }
}

@Composable
private fun NotesTab(build: com.cubicserenity.esobuildmanager.domain.model.Build, vm: BuildEditorViewModel) {
    OutlinedTextField(
        value = build.notes,
        onValueChange = { vm.update { copy(notes = it) } },
        label = { Text("Notes") },
        modifier = Modifier.fillMaxSize().padding(16.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.Sentences,
        ),
    )
}

@Composable
fun DropdownField(label: String, value: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.ifBlank { "—" }) },
                    onClick = { onSelect(option); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun IntField(label: String, value: Int, onValueChange: (Int) -> Unit) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { onValueChange(it.toIntOrNull() ?: value) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}
