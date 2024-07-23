package io.github.annaharri89.stitchcounter.sharedComposables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.annaharri89.stitchcounter.theme.STTheme

@Composable
fun Header(titleId: Int, textColor: Color = STTheme.colors.cBlack, imageVector: ImageVector = Icons.Outlined.LocalLibrary) {
    Surface(
        elevation = STTheme.spaces.xxL,
        shape = STTheme.shapes.l,
        modifier = Modifier
            .fillMaxWidth()
            .padding(STTheme.spaces.l)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Absolute.Center,
            modifier = Modifier.fillMaxWidth().background(STTheme.colors.primary).padding(STTheme.spaces.xxL)) {
            val title = stringResource(id = titleId)
            Icon(
                imageVector = imageVector,
                contentDescription = title,
                tint = textColor,
                modifier = Modifier.size(55.dp).padding(end = STTheme.spaces.m),
            )
            Text(
                text = title,
                style = STTheme.typography.h1,
                color = textColor
            )
        }
    }
}