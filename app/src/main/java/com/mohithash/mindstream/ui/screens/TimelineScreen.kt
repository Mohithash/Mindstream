@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.mindstream.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mohithash.mindstream.domain.MOODS
import com.mohithash.mindstream.ui.AppViewModel
import com.mohithash.mindstream.ui.EmptyState
import com.mohithash.mindstream.ui.Label
import com.mohithash.mindstream.ui.StatCard
import com.mohithash.mindstream.ui.TrendChart
import com.mohithash.mindstream.ui.prettyDate
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TimelineScreen(vm: AppViewModel, onOpen: () -> Unit) {
    val entries by vm.entries.collectAsState()
    val stats by vm.stats.collectAsState()
    val cs = MaterialTheme.colorScheme
    val tf = DateTimeFormatter.ofPattern("HH:mm")
    Scaffold(topBar = { TopAppBar(title = { Text("Timeline") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface)) }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                StatCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Label("Mood · 14 days"); Text(stats.avgMood7?.let { String.format("%.1f / 5 this week", it) } ?: "No entries this week", style = MaterialTheme.typography.titleLarge) }
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) { Label("Streak"); Text("${stats.streak} d", style = MaterialTheme.typography.titleLarge) }
                    }
                    val vals = stats.moodSeries.map { it.second }
                    if (vals.count { it > 0f } >= 2) {
                        // carry the last known mood across empty days so the line stays continuous
                        var last = vals.first { it > 0f }
                        TrendChart(vals.map { v -> if (v > 0f) { last = v; v } else last }, Modifier.padding(top = 8.dp), target = 3f)
                    }
                }
            }
            if (entries.isEmpty()) item { EmptyState(Icons.Default.EditNote, "No entries yet", "Your timeline fills up as you write.") }
            entries.groupBy { it.date }.forEach { (date, list) ->
                item(key = "h$date") { Text(date.prettyDate(), style = MaterialTheme.typography.labelLarge, color = cs.primary, modifier = Modifier.padding(top = 12.dp, bottom = 2.dp)) }
                items(list, key = { it.id }) { e ->
                    ListItem(
                        leadingContent = { Text(MOODS[e.mood - 1].first, style = MaterialTheme.typography.headlineSmall) },
                        headlineContent = { Text(e.text, maxLines = 2) },
                        supportingContent = { Text(listOf(Instant.ofEpochMilli(e.timestamp).atZone(ZoneId.systemDefault()).format(tf), e.tags.replace(",", " · ")).filter { it.isNotBlank() }.joinToString("  ·  ")) },
                        trailingContent = { if (e.reflection.isNotBlank()) Icon(Icons.Default.AutoAwesome, "Reflected", tint = cs.primary) },
                        colors = ListItemDefaults.colors(containerColor = cs.surfaceContainerLow),
                        modifier = Modifier.clip(MaterialTheme.shapes.large).clickable { vm.selected.value = e; vm.clearReflect(); onOpen() },
                    )
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
