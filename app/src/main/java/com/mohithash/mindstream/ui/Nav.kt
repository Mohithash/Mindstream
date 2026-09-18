@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.mindstream.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mohithash.mindstream.ui.screens.EntryScreen
import com.mohithash.mindstream.ui.screens.InsightsScreen
import com.mohithash.mindstream.ui.screens.OnboardingScreen
import com.mohithash.mindstream.ui.screens.SettingsScreen
import com.mohithash.mindstream.ui.screens.TimelineScreen
import com.mohithash.mindstream.ui.screens.WriteScreen

enum class Tab(val route: String, val label: String, val icon: ImageVector, val selected: ImageVector) {
    WRITE("write", "Write", Icons.Outlined.Edit, Icons.Filled.Edit),
    TIMELINE("timeline", "Timeline", Icons.Outlined.Timeline, Icons.Filled.Timeline),
    INSIGHTS("insights", "Insights", Icons.Outlined.Insights, Icons.Filled.Insights),
}

@Composable
fun Nav(vm: AppViewModel) {
    val settings by vm.settings.collectAsState()
    if (!settings.onboarded) { OnboardingScreen(vm); return }
    val nav = rememberNavController()
    val back by nav.currentBackStackEntryAsState()
    val current = back?.destination
    val showBar = Tab.entries.any { t -> current?.hierarchy?.any { it.route == t.route } == true }
    Scaffold(bottomBar = {
        if (showBar) NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
            Tab.entries.forEach { tab ->
                val sel = current?.hierarchy?.any { it.route == tab.route } == true
                NavigationBarItem(selected = sel, onClick = { nav.navigate(tab.route) { popUpTo(nav.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true } },
                    icon = { Icon(if (sel) tab.selected else tab.icon, tab.label) }, label = { Text(tab.label) })
            }
        }
    }) { pad ->
        NavHost(nav, Tab.WRITE.route, Modifier.padding(bottom = pad.calculateBottomPadding())) {
            composable(Tab.WRITE.route) { WriteScreen(vm, onSaved = { nav.navigate("entry") }, onSettings = { nav.navigate("settings") }) }
            composable(Tab.TIMELINE.route) { TimelineScreen(vm, onOpen = { nav.navigate("entry") }) }
            composable(Tab.INSIGHTS.route) { InsightsScreen(vm, onSettings = { nav.navigate("settings") }) }
            composable("entry") { EntryScreen(vm, onBack = { nav.popBackStack() }) }
            composable("settings") { SettingsScreen(vm, onBack = { nav.popBackStack() }) }
        }
    }
}
