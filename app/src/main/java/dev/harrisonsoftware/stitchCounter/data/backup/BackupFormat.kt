package dev.harrisonsoftware.stitchCounter.data.backup

import com.google.gson.annotations.SerializedName

data class BackupMetadata(
    @SerializedName("version")
    val version: Int = 1,
    @SerializedName("export_date")
    val exportDate: Long,
    @SerializedName("app_version")
    val appVersion: String,
    @SerializedName("project_count")
    val projectCount: Int
)

data class BackupProject(
    @SerializedName("id")
    val id: Int,
    @SerializedName("type")
    val type: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("stitch_counter_number")
    val stitchCounterNumber: Int,
    @SerializedName("stitch_adjustment")
    val stitchAdjustment: Int,
    @SerializedName("row_counter_number")
    val rowCounterNumber: Int,
    @SerializedName("row_adjustment")
    val rowAdjustment: Int,
    @SerializedName("total_rows")
    val totalRows: Int,
    @SerializedName("image_paths")
    val imagePaths: List<String>
)

data class BackupData(
    @SerializedName("metadata")
    val metadata: BackupMetadata,
    @SerializedName("projects")
    val projects: List<BackupProject>
)
