package dev.harrisonsoftware.stitchCounter.domain.usecase

import dev.harrisonsoftware.stitchCounter.data.repo.ProjectRepository
import dev.harrisonsoftware.stitchCounter.domain.mapper.toDomain
import dev.harrisonsoftware.stitchCounter.domain.mapper.toEntity
import dev.harrisonsoftware.stitchCounter.domain.model.Project
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveProjects @Inject constructor(
    private val repo: ProjectRepository
) {
    operator fun invoke(): Flow<List<Project>> = repo.observeProjects().map { list -> list.map { it.toDomain() } }
}

@Singleton
class GetProject @Inject constructor(
    private val repo: ProjectRepository
) {
    suspend operator fun invoke(id: Int): Project? = repo.getProject(id)?.toDomain()
}

@Singleton
class UpsertProject @Inject constructor(
    private val repo: ProjectRepository
) {
    suspend operator fun invoke(project: Project): Long = repo.upsert(project.toEntity())
}

@Singleton
class DeleteProject @Inject constructor(
    private val repo: ProjectRepository
) {
    suspend operator fun invoke(project: Project) = repo.delete(project.toEntity())
}

@Singleton
class DeleteProjects @Inject constructor(
    private val repo: ProjectRepository
) {
    suspend operator fun invoke(projects: List<Project>) {
        if (projects.isNotEmpty()) {
            repo.deleteByIds(projects.map { it.id })
        }
    }
}

@Singleton
class UpdateSingleCounterValues @Inject constructor(
    private val repo: ProjectRepository
) {
    suspend operator fun invoke(
        id: Int,
        stitchCount: Int,
        stitchAdjustment: Int,
        totalStitchesEver: Int,
        updatedAt: Long
    ) = repo.updateSingleCounterValues(id, stitchCount, stitchAdjustment, totalStitchesEver, updatedAt)
}

@Singleton
class UpdateDoubleCounterValues @Inject constructor(
    private val repo: ProjectRepository
) {
    suspend operator fun invoke(
        id: Int,
        stitchCount: Int,
        stitchAdjustment: Int,
        rowCount: Int,
        rowAdjustment: Int,
        totalStitchesEver: Int,
        updatedAt: Long
    ) = repo.updateDoubleCounterValues(id, stitchCount, stitchAdjustment, rowCount, rowAdjustment, totalStitchesEver, updatedAt)
}
