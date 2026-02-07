package dev.harrisonsoftware.stitchCounter.feature.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.harrisonsoftware.stitchCounter.feature.navigation.RootNavigationViewModel
import dev.harrisonsoftware.stitchCounter.feature.navigation.SheetScreen
import dev.harrisonsoftware.stitchCounter.feature.navigation.RootNavGraph
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
            Text("Stitch Counter", style = MaterialTheme.typography.headlineMedium)

            Button(onClick = {
                viewModel.showBottomSheet(SheetScreen.ProjectDetail(projectId = null, projectType = dev.harrisonsoftware.stitchCounter.domain.model.ProjectType.SINGLE))
            }) {
                Text("New Single Tracker")
            }

            Button(onClick = {
                viewModel.showBottomSheet(SheetScreen.ProjectDetail(projectId = null, projectType = dev.harrisonsoftware.stitchCounter.domain.model.ProjectType.DOUBLE))
            }) {
                Text("New Double Tracker")
            }
        }
    }
}