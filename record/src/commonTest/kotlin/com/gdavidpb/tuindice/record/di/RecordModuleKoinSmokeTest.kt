package com.gdavidpb.tuindice.record.di

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.record.data.mutation.AcademicRecordMutation
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import com.gdavidpb.tuindice.record.domain.repository.RecordSelectionRepository
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import kotlin.test.Test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.koin.dsl.module

class RecordModuleKoinSmokeTest {
	@Test
	fun resolvesRecordViewModel() = withKoinSmokeTest(
		recordModule,
		module {
			single<AcademicRecordRepository> { StubAcademicRecordRepository() }
			single<RecordSelectionRepository> { StubRecordSelectionRepository() }
			single<ReportingRepository> { RecordingReportingRepository() }
		}
	) {
		assertResolves(RecordViewModel::class)
	}
}

private class StubAcademicRecordRepository : AcademicRecordRepository {
	override suspend fun observeAcademicRecordFlow(): Flow<AcademicRecord> = emptyFlow()

	override suspend fun getAcademicRecord(): AcademicRecord? = null

	override suspend fun updateAcademicRecord() = Unit

	override suspend fun drainPendingMutations() = Unit

	override suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?,
		commit: Boolean
	) = Unit

	override suspend fun deleteAttemptOverride(attemptId: String) = Unit

	override suspend fun addSyntheticTerm(command: AcademicRecordMutation.AddSyntheticTerm) = Unit

	override suspend fun deleteSyntheticTerm(termId: String) = Unit
}

private class StubRecordSelectionRepository : RecordSelectionRepository {
	override fun observeSelectedTermId(viewMode: RecordViewMode): Flow<String?> = flowOf(null)

	override fun observeRecordViewMode(): Flow<RecordViewMode> = flowOf(RecordViewMode.Working)

	override suspend fun getSelectedTermId(viewMode: RecordViewMode): String? = null

	override suspend fun setSelectedTermId(viewMode: RecordViewMode, termId: String) = Unit

	override suspend fun getRecordViewMode(): RecordViewMode = RecordViewMode.Working

	override suspend fun setRecordViewMode(viewMode: RecordViewMode) = Unit
}
