package io.github.annaharri89.stitchcounter.sharedComposables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.annaharri89.stitchcounter.theme.STTheme

@Composable
fun Card(content: @Composable () -> Unit) {
    Surface(
        elevation = STTheme.spaces.m,
        shape = STTheme.shapes.l,
        color = STTheme.colors.listItem,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = STTheme.spaces.l,
                end = STTheme.spaces.l,
                top = STTheme.spaces.s,
                bottom = STTheme.spaces.s
            ), content = content)
}