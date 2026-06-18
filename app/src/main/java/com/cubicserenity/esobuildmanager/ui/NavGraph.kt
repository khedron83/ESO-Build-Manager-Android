package com.cubicserenity.esobuildmanager.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cubicserenity.esobuildmanager.ui.builds.BuildsScreen
import com.cubicserenity.esobuildmanager.ui.detail.BuildDetailScreen
import com.cubicserenity.esobuildmanager.ui.editor.BuildEditorScreen
import com.cubicserenity.esobuildmanager.ui.settings.SettingsScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "builds") {
        composable("builds") {
            BuildsScreen(
                onBuildClick = { id -> navController.navigate("detail/$id") },
                onNewBuild = { navController.navigate("editor/0") },
                onOpenSettings = { navController.navigate("settings") },
            )
        }
        composable(
            "detail/{buildId}",
            arguments = listOf(navArgument("buildId") { type = NavType.LongType }),
        ) { back ->
            val id = back.arguments!!.getLong("buildId")
            BuildDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate("editor/$id") },
            )
        }
        composable(
            "editor/{buildId}",
            arguments = listOf(navArgument("buildId") { type = NavType.LongType; defaultValue = 0L }),
        ) { back ->
            val id = back.arguments!!.getLong("buildId")
            BuildEditorScreen(
                onBack = { navController.popBackStack() },
                onSaved = {
                    navController.popBackStack()
                    if (id == 0L) navController.popBackStack()
                },
            )
        }
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
