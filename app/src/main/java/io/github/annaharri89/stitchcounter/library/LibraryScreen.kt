package io.github.annaharri89.stitchcounter.library

import android.content.Intent
import android.database.Cursor
import android.database.DatabaseUtils
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.spec.DestinationStyle
import io.github.annaharri89.stitchcounter.R
import io.github.annaharri89.stitchcounter.dataObjects.Counter
import io.github.annaharri89.stitchcounter.dataObjects.StyledTextData
import io.github.annaharri89.stitchcounter.enums.DBFields
import io.github.annaharri89.stitchcounter.enums.ProjectTypes
import io.github.annaharri89.stitchcounter.main.OldMainActivity
import io.github.annaharri89.stitchcounter.navigation.StitchTrackerNavGraph
import io.github.annaharri89.stitchcounter.sharedComposables.Card
import io.github.annaharri89.stitchcounter.sharedComposables.Header
import io.github.annaharri89.stitchcounter.sharedComposables.StyledText
import io.github.annaharri89.stitchcounter.theme.STTheme
import io.github.annaharri89.stitchcounter.utilities.capitalized

@Preview
@StitchTrackerNavGraph(start = true)
@Destination(style = DestinationStyle.Default::class)
@Composable
fun LibraryScreen(tempCursor: Cursor? = null) {
    Log.i(
        "composeLibrary",
        "tempCursor $tempCursor"
    )
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
                Header(titleId = R.string.action_library)
                LazyColumn(modifier = Modifier
                    .fillMaxSize()) {
                    tempCursor?.let { cursor: Cursor ->
                        try {
                            val shouldAccessCursor = cursor.moveToFirst()
                            Log.i(
                                "composeLibrary",
                                "LazyColumn, annaData ${DatabaseUtils.dumpCursorToString(cursor)}"
                            )
                            if (shouldAccessCursor) {
                                do {
                                    val id = cursor.getInt(DBFields.ID.index)
                                    val type = cursor.getString(DBFields.TYPE.index)
                                    val projectTitle = cursor.getString(DBFields.TITLE.index)
                                    val stitchCounterNumber =
                                        cursor.getInt(DBFields.STITCH_COUNTER_NUMBER.index)
                                    val stitchAdjustment =
                                        cursor.getInt(DBFields.STITCH_ADJUSTMENT.index)
                                    val rowCounterNumber =
                                        cursor.getInt(DBFields.ROW_COUNTER_NUMBER.index)
                                    val rowAdjustment =
                                        cursor.getInt(DBFields.ROW_ADJUSTMENT.index)
                                    val maxRows = cursor.getInt(DBFields.TOTAL_ROWS.index)
                                    val counter = Counter(
                                        id = id,
                                        type = type,
                                        name = projectTitle,
                                        rowCounterNumber = rowCounterNumber,
                                        stitchCounterNumber = stitchCounterNumber,
                                        stitchAdjustment = stitchAdjustment,
                                        rowAdjustment = rowAdjustment,
                                        totalRows = maxRows
                                    )
                                    val progress =
                                        (rowCounterNumber.toFloat() / maxRows.toFloat()) * 100
                                    item {
                                            Card {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(STTheme.spaces.l)
                                                ) {
                                                    Row(modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            //onListItemClicked.invoke(counter)//todo
                                                        }) {
                                                        Column {
                                                            Text(
                                                                text = projectTitle.capitalized(),
                                                                style = STTheme.typography.subtitle3,
                                                                color = STTheme.colors.textPrimary
                                                            )
                                                            if (type == ProjectTypes.DOUBLE.name) {
                                                                Text(
                                                                    text = "Total Rows: $maxRows",
                                                                    style = STTheme.typography.body5,
                                                                    color = STTheme.colors.textSecondary
                                                                )
                                                                Text(
                                                                    text = "Progress: $progress%",
                                                                    style = STTheme.typography.body5,
                                                                    color = STTheme.colors.textSecondary
                                                                )
                                                            }
                                                        }

                                                    }
                                                }
                                            }
                                    }
                                } while (cursor.moveToNext())
                            } else {
                                item {
                                    Card {
                                        Column(
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(STTheme.spaces.xxL)
                                        ) {
                                            val loraSubtitle = SpanStyle(
                                                fontFamily = STTheme.typography.subtitle1.fontFamily,
                                                fontSize = STTheme.typography.subtitle1.fontSize,
                                                color = STTheme.colors.textPrimary,
                                                fontWeight = STTheme.typography.subtitle1.fontWeight
                                            )
                                            val dancingSubtitle = SpanStyle(
                                                fontFamily = STTheme.typography.h4.fontFamily,
                                                color = STTheme.colors.textPrimary,
                                                fontSize = STTheme.typography.h4.fontSize,
                                                fontWeight = FontWeight.W900
                                            )
                                            StyledText(
                                                data = listOf(
                                                    StyledTextData(
                                                        text = stringResource(id = R.string.app_desc_1),
                                                        style = loraSubtitle
                                                    ),
                                                    StyledTextData(
                                                        text = stringResource(id = R.string.app_desc_2),
                                                        style = loraSubtitle
                                                    ),
                                                    StyledTextData(
                                                        text = stringResource(id = R.string.app_name),
                                                        style = dancingSubtitle
                                                    ),
                                                    StyledTextData(
                                                        text = stringResource(id = R.string.app_desc_3),
                                                        style = loraSubtitle
                                                    ),
                                                    StyledTextData(
                                                        text = stringResource(id = R.string.app_name),
                                                        style = dancingSubtitle
                                                    ),
                                                    StyledTextData(
                                                        text = stringResource(id = R.string.app_desc_4),
                                                        style = loraSubtitle
                                                    ),
                                                    StyledTextData(
                                                        text = stringResource(id = R.string.action_library),
                                                        style = dancingSubtitle
                                                    ),
                                                    StyledTextData(
                                                        text = stringResource(id = R.string.app_desc_5),
                                                        style = loraSubtitle
                                                    ),
                                                    StyledTextData(
                                                        text = stringResource(id = R.string.app_name),
                                                        style = dancingSubtitle
                                                    ),
                                                    StyledTextData(
                                                        text = stringResource(id = R.string.app_desc_6),
                                                        style = loraSubtitle
                                                    ),
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        } finally {
                            //cursor.close()//todo stitchCounterV2
                        }
                    }
                }

            }

            val context = LocalContext.current
            FloatingActionButton(
                modifier = Modifier.padding(bottom = 40.dp, end = 40.dp),
                onClick = {
                    val i = Intent(context, OldMainActivity::class.java)
                    context.startActivity(i)
                },
                backgroundColor = STTheme.colors.accentDark,
                contentColor = STTheme.colors.cWhite
            ) {
                Icon(Icons.Filled.Add, "Add")
            }
        }

}

