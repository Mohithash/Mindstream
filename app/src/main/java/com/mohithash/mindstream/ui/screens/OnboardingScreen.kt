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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.mohithash.mindstream.domain.Settings
import com.mohithash.mindstream.ui.AppViewModel
import com.mohithash.mindstream.ui.Label
import com.mohithash.mindstream.ui.StatCard

@Composable
fun SettingsForm(initial: Settings, onChange: (Settings) -> Unit) {
    var name by remember { mutableStateOf(initial.name) }
    var tone by remember { mutableStateOf(initial.tone) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(name, { name = it; onChange(initial.copy(name = it, tone = tone)) }, label = { Text("What should I call you? (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large)
        Label("Companion tone")
        Row(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
            listOf("warm", "direct", "playful").forEachIndexed { i, t ->
                ToggleButton(checked = tone == t, onCheckedChange = { tone = t; onChange(initial.copy(name = name, tone = t)) }, modifier = Modifier.weight(1f),
                    shapes = when (i) { 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes(); 2 -> ButtonGroupDefaults.connectedTrailingButtonShapes(); else -> ButtonGroupDefaults.connectedMiddleButtonShapes() }) { Text(t.replaceFirstChar { it.uppercase() }) }
            }
        }
    }
}

@Composable
fun OnboardingScreen(vm: AppViewModel) {
    var draft by remember { mutableStateOf(Settings()) }
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = { LargeFlexibleTopAppBar(title = { Text("A quiet place to think") }, subtitle = { Text("Write daily. Get a gentle reflection back. Spot the patterns in your weeks.") }, scrollBehavior = scroll) }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard { SettingsForm(Settings()) { draft = it } }
            Text("Everything stays on your phone. AI reflections only happen when you tap the button, using your own API key.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button({ vm.saveSettings(draft) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Begin", style = MaterialTheme.typography.titleMedium) }
            Spacer(Modifier.height(24.dp))
        }
    }
}
