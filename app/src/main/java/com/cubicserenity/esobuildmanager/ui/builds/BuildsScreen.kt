package com.cubicserenity.esobuildmanager.ui.builds

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cubicserenity.esobuildmanager.domain.model.Build
import com.cubicserenity.esobuildmanager.util.ROLES

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildsScreen(
    onBuildClick: (Long) -> Unit,
    onNewBuild: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BuildsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var searchActive by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    var deleteTarget by remember { mutableStateOf<Build?>(null) }

    LaunchedEffect(searchActive) {
        if (searchActive) focusRequester.requestFocus()
    }

    state.syncResult?.let { result ->
        val msg = buildString {
            if (result.uploaded > 0) append("${result.uploaded} uploaded")
            if (result.downloaded > 0) {
                if (isNotEmpty()) append(", ")
                append("${result.downloaded} downloaded")
            }
            if (result.uploaded == 0 && result.downloaded == 0) append("Nothing to sync")
            if (result.errors.isNotEmpty()) append(" (${result.errors.size} errors)")
        }
        LaunchedEffect(result) {
            kotlinx.coroutines.delay(4000)
            viewModel.clearSyncResult()
        }
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = { TextButton(onClick = viewModel::clearSyncResult) { Text("Dismiss") } },
        ) { Text(msg) }
    }

    deleteTarget?.let { build ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Build") },
            text = { Text("Delete \"${build.name}\"?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteBuild(build.id); deleteTarget = null }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } },
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    AnimatedContent(targetState = searchActive, label = "search") { active ->
                        if (active) {
                            OutlinedTextField(
                                value = state.query,
                                onValueChange = viewModel::setQuery,
                                placeholder = { Text("Search builds…") },
                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                            )
                        } else {
                            Text("ESO Build Manager")
                        }
                    }
                },
                actions = {
                    if (searchActive) {
                        IconButton(onClick = { viewModel.setQuery(""); searchActive = false; keyboard?.hide() }) {
                            Icon(Icons.Default.Close, "Close search")
                        }
                    } else {
                        IconButton(onClick = { searchActive = true }) {
                            Icon(Icons.Default.Search, "Search")
                        }
                        if (state.isSyncing) {
                            CircularProgressIndicator(Modifier.size(24.dp).padding(4.dp))
                        } else {
                            IconButton(onClick = viewModel::sync) {
                                Icon(Icons.Default.Sync, "Sync")
                            }
                        }
                        IconButton(onClick = onOpenSettings) {
                            Icon(Icons.Default.Settings, "Settings")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewBuild) {
                Icon(Icons.Default.Add, "New Build")
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Role filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(ROLES) { role ->
                    FilterChip(
                        selected = state.roleFilter == role,
                        onClick = { viewModel.setRoleFilter(role) },
                        label = { Text(role) },
                    )
                }
            }

            when {
                state.filtered.isEmpty() && state.allBuilds.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.Shield, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.secondary)
                            Text("No builds yet", style = MaterialTheme.typography.titleMedium)
                            Text("Tap + to create your first build.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
                state.filtered.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No builds match the filter.")
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(state.filtered, key = { it.id }) { build ->
                            BuildCard(
                                build = build,
                                onClick = { onBuildClick(build.id) },
                                onDelete = { deleteTarget = build },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BuildCard(build: Build, onClick: () -> Unit, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(build.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val tags = listOfNotNull(
                    build.esoClass.takeIf { it.isNotBlank() },
                    build.role.takeIf { it.isNotBlank() },
                    build.content.takeIf { it.isNotBlank() },
                    build.gamePatch.takeIf { it.isNotBlank() },
                )
                if (tags.isNotEmpty()) {
                    Text(
                        tags.joinToString("  ·  "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, "More")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        leadingIcon = { Icon(Icons.Default.Delete, null) },
                        onClick = { showMenu = false; onDelete() },
                    )
                }
            }
        }
    }
}
