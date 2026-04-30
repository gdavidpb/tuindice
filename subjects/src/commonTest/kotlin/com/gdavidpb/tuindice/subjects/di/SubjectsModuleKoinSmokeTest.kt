package com.gdavidpb.tuindice.subjects.di

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailResult
import com.gdavidpb.tuindice.subjects.domain.repository.SubjectStatsRepository
import com.gdavidpb.tuindice.subjects.presentation.viewmodel.SubjectDetailViewModel
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import kotlin.test.Test
import org.koin.dsl.module

class SubjectsModuleKoinSmokeTest {
	@Test
	fun resolvesSubjectDetailViewModel() = withKoinSmokeTest(
		subjectsModule,
		module {
			single<SubjectStatsRepository> { FakeSubjectStatsRepository() }
			single<ReportingRepository> { RecordingReportingRepository() }
		}
	) {
		assertResolves(SubjectDetailViewModel::class)
	}
}

private class FakeSubjectStatsRepository : SubjectStatsRepository {
	override suspend fun getFreshSubjectDetail(subjectCode: String): SubjectDetailResult? {
		error("This smoke test should not execute repository calls.")
	}

	override suspend fun refreshSubjectDetail(subjectCode: String): SubjectDetailResult {
		error("This smoke test should not execute repository calls.")
	}
}
