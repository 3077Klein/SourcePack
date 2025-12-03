package org.source.pack

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.composefluent.FluentTheme
import io.github.composefluent.background.Mica
import org.source.pack.ui.Home

@Composable
fun App() {
    FluentTheme {
        Mica(Modifier.fillMaxSize()) {
            Home()
        }
    }
}