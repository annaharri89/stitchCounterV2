package io.github.annaharri89.stitchcounter.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ImportExport
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.spec.DestinationStyle
import io.github.annaharri89.stitchcounter.R
import io.github.annaharri89.stitchcounter.main.MainViewModel
import io.github.annaharri89.stitchcounter.navigation.StitchTrackerNavGraph
import io.github.annaharri89.stitchcounter.sharedComposables.Button
import io.github.annaharri89.stitchcounter.sharedComposables.Header
import io.github.annaharri89.stitchcounter.theme.LocalColors
import io.github.annaharri89.stitchcounter.theme.STTheme
import io.github.annaharri89.stitchcounter.theme.retroSummerDarkColors
import io.github.annaharri89.stitchcounter.theme.retroSummerLightColors

//@Preview//todo stitchCounterV2
@StitchTrackerNavGraph()
@Destination(style = DestinationStyle.Default::class)
@Composable
fun SettingsScreen(mainViewModel: MainViewModel) {
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
                Header(titleId = R.string.action_settings, imageVector = Icons.Outlined.Settings)
                //val currentColor = remember { if (darkColors != null && darkTheme) darkColors else colors }
                //val rememberedColors = remember { currentColor.copy() }.apply { updateColorsFrom(currentColor) }
                Button(titleId = R.string.change,
                    onClick = {
                        mainViewModel.addLightColors(retroSummerLightColors())
                        mainViewModel.addDarkColors(retroSummerDarkColors())
                    },
                    color = Color.Green)
            }
        }

}

