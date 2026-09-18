@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.mindstream.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohithash.mindstream.ui.AppViewModel
import com.mohithash.mindstream.ui.EmptyState
import com.mohithash.mindstream.ui.HeroCard
import com.mohithash.mindstream.ui.Job
import com.mohithash.mindstream.ui.Label
import com.mohithash.mindstream.ui.ShapeIcon
import com.mohithash.mindstream.ui.StatCard
import com.mohithash.mindstream.ui.theme.Brand
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun InsightsScreen(vm: AppViewModel, onSettings: () -> Unit) {
    val weekly by vm.weekly.collectAsState()
    val job by vm.weeklyJob.collectAsState()
    val ai by vm.ai.collectAsState()
    val cs = MaterialTheme.colorScheme
    Scaffold(topBar = { TopAppBar(title = { Text("Weekly insight") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface), actions = { IconButton(onSettings) { Icon(Icons.Default.Settings, null) } }) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button({ vm.generateWeekly() }, enabled = ai.configured && job != Job.Loading, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth().height(52.dp)) {
                if (job == Job.Loading) { LoadingIndicator(Modifier.size(20.dp)); Spacer(Modifier.size(8.dp)); Text("Reading your week…") }
                else { Icon(Icons.Default.AutoAwesome, null); Spacer(Modifier.size(8.dp)); Text(if (weekly.generatedAt == 0L) "Generate this week's insight" else "Regenerate") }
            }
            (job as? Job.Failed)?.let { StatCard(container = cs.errorContainer) { Text(it.message, color = cs.onErrorContainer) } }
            if (weekly.generatedAt == 0L) EmptyState(Icons.Default.Insights, "No insight yet", "After a few entries, get a weekly read on your patterns, wins and one experiment to try.")
            else {
                HeroCard(colors = listOf(cs.primary, Brand.heroDeep), blobShape = MaterialShapes.Cookie12Sided) {
                    val on = cs.onPrimary
                    Label("${weekly.entryCount} entries · ${Instant.ofEpochMilli(weekly.generatedAt).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("d MMM"))}", on.copy(alpha = 0.8f))
                    Text(weekly.headline, style = MaterialTheme.typography.headlineSmall, color = on)
                }
                Section(Icons.Default.Insights, "Patterns", weekly.patterns, cs.surfaceContainerLow, cs.onSurface)
                Section(Icons.Default.EmojiEvents, "Wins", weekly.wins, cs.secondaryContainer, cs.onSecondaryContainer)
                Section(Icons.Default.Shield, "Be gentle about", listOf(weekly.watch_out), cs.tertiaryContainer, cs.onTertiaryContainer)
                Section(Icons.Default.Science, "Try next week", listOf(weekly.suggestion), cs.primaryContainer, cs.onPrimaryContainer)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Section(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, items: List<String>, bg: androidx.compose.ui.graphics.Color, fg: androidx.compose.ui.graphics.Color) {
    if (items.all { it.isBlank() }) return
    StatCard(container = bg) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ShapeIcon(icon, fg.copy(alpha = 0.12f), fg, MaterialShapes.Sunny); Text(title, style = MaterialTheme.typography.titleMedium, color = fg)
        }
        items.filter { it.isNotBlank() }.forEach { Text("•  $it", style = MaterialTheme.typography.bodyLarge, color = fg, modifier = Modifier.padding(start = 4.dp, top = 2.dp)) }
    }
}
