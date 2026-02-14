package dev.harrisonsoftware.stitchCounter.data.backup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BackupMetadata(
    @SerialName("version")
    val version: Int = 1,
    @SerialName("export_date")
    val exportDate: Long,
    @SerialName("app_version")
    val appVersion: String,
    @SerialName("project_count")
    val projectCount: Int
)

@Serializable
data class BackupProject(
    @SerialName("id")
    val id: Int,
    @SerialName("type")
    val type: String,
    @SerialName("title")
    val title: String,
    @SerialName("stitch_counter_number")
    val stitchCounterNumber: Int,
    @SerialName("stitch_adjustment")
    val stitchAdjustment: Int,
    @SerialName("row_counter_number")
    val rowCounterNumber: Int,
    @SerialName("row_adjustment")
    val rowAdjustment: Int,
    @SerialName("total_rows")
    val totalRows: Int,
    @SerialName("image_paths")
    val imagePaths: List<String>
)

@Serializable
data class BackupData(
    @SerialName("metadata")
    val metadata: BackupMetadata,
    @SerialName("projects")
    val projects: List<BackupProject>
)
