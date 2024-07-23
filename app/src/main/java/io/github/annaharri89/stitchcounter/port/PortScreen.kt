package io.github.annaharri89.stitchcounter.port

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ImportExport
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.spec.DestinationStyle
import io.github.annaharri89.stitchcounter.R
import io.github.annaharri89.stitchcounter.navigation.StitchTrackerNavGraph
import io.github.annaharri89.stitchcounter.sharedComposables.Header
import io.github.annaharri89.stitchcounter.theme.STTheme

@Preview
@StitchTrackerNavGraph()
@Destination(style = DestinationStyle.Default::class)
@Composable
fun PortScreen() {
        Box(modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd) {
            Column(modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            STTheme.colors.secondary,
                            STTheme.colors.accentLight,
                            STTheme.colors.primary,
                        )
                    )
                )) {
                Header(titleId = R.string.action_port, imageVector = Icons.Outlined.ImportExport)

            }
        }
}

