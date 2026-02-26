package dev.harrisonsoftware.stitchCounter.feature.projectDetail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.harrisonsoftware.stitchCounter.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImagePreviewBottomSheet(
    imagePaths: List<String>,
    initialPageIndex: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(
        initialPage = initialPageIndex.coerceIn(0, (imagePaths.size - 1).coerceAtLeast(0)),
        pageCount = { imagePaths.size }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        ImagePreviewTopBar(
            currentPage = pagerState.currentPage + 1,
            totalPages = imagePaths.size,
            onClose = onDismiss
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            key = { imagePaths[it] }
        ) { pageIndex ->
            FullSizeProjectImage(
                imagePath = imagePaths[pageIndex],
                pageNumber = pageIndex + 1,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (imagePaths.size > 1) {
            PageIndicatorRow(
                currentPage = pagerState.currentPage,
                pageCount = imagePaths.size,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
        }
    }
}

@Composable
private fun ImagePreviewTopBar(
    currentPage: Int,
    totalPages: Int,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.image_preview_page_indicator, currentPage, totalPages),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 8.dp)
        )
        IconButton(
            onClick = onClose,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.cd_close_image_preview),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FullSizeProjectImage(
    imagePath: String,
    pageNumber: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageDescription = stringResource(R.string.cd_full_size_project_image, pageNumber)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(resolveImagePathToAbsolutePath(context, imagePath))
                .crossfade(true)
                .build(),
            contentDescription = imageDescription,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun PageIndicatorRow(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier
) {
    val indicatorDescription = stringResource(
        R.string.cd_image_page_indicator,
        currentPage + 1,
        pageCount
    )

    Row(
        modifier = modifier.semantics { contentDescription = indicatorDescription },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (isSelected) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
            )
        }
    }
}
