package dev.harrisonsoftware.stitchCounter.domain.model

data class Note(
    val id: Int = 0,
    val title: String,
    val body: String,
    val createdAt: Long,
    val updatedAt: Long,
)
