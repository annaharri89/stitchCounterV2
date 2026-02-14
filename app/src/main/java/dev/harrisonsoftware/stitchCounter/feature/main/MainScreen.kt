package dev.harrisonsoftware.stitchCounter.feature.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.harrisonsoftware.stitchCounter.R
import dev.harrisonsoftware.stitchCounter.feature.navigation.RootNavigationViewModel
import dev.harrisonsoftware.stitchCounter.feature.navigation.SheetScreen
import dev.harrisonsoftware.stitchCounter.feature.navigation.RootNavGraph
import dev.harrisonsoftware.stitchCounter.feature.sharedComposables.AppButton
import com.ramcosta.composedestinations.annotation.Destination

@RootNavGraph(start = true)
@Destination
@Composable
fun MainScreen(
    viewModel: RootNavigationViewModel
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.main_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics { heading() }
            )

            AppButton(
                onClick = {
                    viewModel.showBottomSheet(
                        SheetScreen.ProjectDetail(
                            projectId = null,
                            projectType = dev.harrisonsoftware.stitchCounter.domain.model.ProjectType.SINGLE
                        )
                    )
                },
            ) {
                Text(stringResource(R.string.main_new_single_tracker))
            }

            AppButton(
                onClick = {
                    viewModel.showBottomSheet(
                        SheetScreen.ProjectDetail(
                            projectId = null,
                            projectType = dev.harrisonsoftware.stitchCounter.domain.model.ProjectType.DOUBLE
                        )
                    )
                },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
            ) {
                Text(stringResource(R.string.main_new_double_tracker))
            }
        }
    }
}
