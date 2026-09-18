@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package com.mohithash.mindstream.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.mohithash.mindstream.domain.MOODS
import com.mohithash.mindstream.domain.TAGS
import com.mohithash.mindstream.ui.AppViewModel
import com.mohithash.mindstream.ui.HeroCard
import com.mohithash.mindstream.ui.Job
import com.mohithash.mindstream.ui.Label
import com.mohithash.mindstream.ui.StatCard
import com.mohithash.mindstream.ui.theme.Brand
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun MoodPicker(mood: Int, onPick: (Int) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        MOODS.forEachIndexed { i, (emoji, label) ->
            val sel = mood == i + 1
            val bg by animateColorAsState(if (sel) cs.primary else cs.surfaceContainerHigh, label = "bg")
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(Modifier.size(56.dp).scale(if (sel) 1.12f else 1f).clip(MaterialShapes.Cookie9Sided.toShape()).background(bg).clickable { onPick(i + 1) }, contentAlignment = Alignment.Center) {
                    Text(emoji, style = MaterialTheme.typography.headlineSmall)
                }
                Text(label, style = MaterialTheme.typography.labelSmall, color = if (sel) cs.primary else cs.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun WriteScreen(vm: AppViewModel, onSaved: () -> Unit, onSettings: () -> Unit) {
    val stats by vm.stats.collectAsState()
    val ai by vm.ai.collectAsState()
    val prompt by vm.prompt.collectAsState()
    val promptJob by vm.promptJob.collectAsState()
    val cs = MaterialTheme.colorScheme
    var text by remember { mutableStateOf("") }
    var mood by remember { mutableIntStateOf(3) }
    var tags by remember { mutableStateOf(setOf<String>()) }
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(title = { Text(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM"))) },
                subtitle = { Text(if (stats.streak > 0) "${stats.streak}‑day streak · ${stats.total} entries" else "Start your streak today") },
                actions = { IconButton(onSettings) { Icon(Icons.Default.Settings, null) } }, scrollBehavior = scroll,
                colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface, scrolledContainerColor = cs.surface))
        },
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            HeroCard(colors = listOf(cs.primary, Brand.heroDeep), blobShape = MaterialShapes.Clover8Leaf) {
                val on = cs.onPrimary
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Label("Today's prompt", on.copy(alpha = 0.8f))
                    IconButton({ vm.newPrompt(useAi = true) }) { if (promptJob == Job.Loading) LoadingIndicator(Modifier.size(20.dp), color = on) else Icon(Icons.Default.Refresh, "New prompt", tint = on) }
                }
                Text(prompt, style = MaterialTheme.typography.headlineSmall, color = on)
                if (stats.streak >= 3) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocalFireDepartment, null, tint = cs.secondaryContainer, modifier = Modifier.size(16.dp)); Text("${stats.streak} days in a row", color = on.copy(alpha = 0.85f), style = MaterialTheme.typography.labelLarge)
                }
            }
            StatCard {
                Label("How are you?")
                MoodPicker(mood) { mood = it }
            }
            StatCard {
                OutlinedTextField(text, { text = it }, modifier = Modifier.fillMaxWidth(), minLines = 6, shape = MaterialTheme.shapes.large,
                    placeholder = { Text("Write freely. Nobody's reading but you (and, only if you ask, your AI).") })
                Label("Tags")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TAGS.forEach { t -> FilterChip(selected = t in tags, onClick = { tags = if (t in tags) tags - t else tags + t }, label = { Text(t) }) }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { vm.save(text, mood, tags.toList(), false); text = ""; tags = emptySet(); onSaved() }, enabled = text.isNotBlank(), shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f).height(52.dp)) { Text("Save") }
                Button(onClick = { vm.save(text, mood, tags.toList(), true); text = ""; tags = emptySet(); onSaved() }, enabled = text.isNotBlank() && ai.configured, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(2f).height(52.dp)) {
                    Icon(Icons.Default.AutoAwesome, null); Spacer(Modifier.size(8.dp)); Text("Save & reflect")
                }
            }
            if (!ai.configured) Text("Add an API key in Settings to unlock reflections and weekly insights.", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
        }
    }
}
