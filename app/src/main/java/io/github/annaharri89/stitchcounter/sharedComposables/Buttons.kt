package io.github.annaharri89.stitchcounter.sharedComposables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import io.github.annaharri89.stitchcounter.theme.STTheme
import kotlinx.coroutines.flow.asFlow

@Composable
fun Button(titleId: Int, onClick: () -> Unit, color: Color) {
    Surface(
        elevation = STTheme.spaces.xxL,
        shape = STTheme.shapes.l,
        modifier = Modifier
            .fillMaxWidth()
            .padding(STTheme.spaces.xxL)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Absolute.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onClick.invoke()
                }
                .background(color)
                .padding(STTheme.spaces.m)) {
            val title = stringResource(id = titleId)
            Text(
                text = title,
                style = STTheme.typography.subtitle4,
                color = STTheme.colors.cWhite
            )
        }
    }
}