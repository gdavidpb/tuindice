package com.gdavidpb.tuindice.subjects.di

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailResult
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSearchResult
import com.gdavidpb.tuindice.subjects.domain.repository.SubjectCatalogRepository
import com.gdavidpb.tuindice.subjects.domain.repository.SubjectStatsRepository
import com.gdavidpb.tuindice.subjects.presentation.viewmodel.SubjectDetailViewModel
import com.gdavidpb.tuindice.subjects.presentation.viewmodel.SubjectSearchViewModel
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlin.test.Test
import org.koin.dsl.module

class SubjectsModuleKoinSmokeTest {
	@Test
	fun resolvesSubjectDetailViewModel() = withKoinSmokeTest(
		subjectsModule,
		module {
			single<SubjectStatsRepository> { FakeSubjectStatsRepository() }
			single<ReportingRepository> { RecordingReportingRepository() }
			single<EventPublisher> { NoOpEventPublisher }
			single<TuIndiceDispatchers> { DefaultTuIndiceDispatchers }
		}
	) {
		assertResolves(SubjectDetailViewModel::class)
	}

	@Test
	fun resolvesSubjectSearchViewModel() = withKoinSmokeTest(
		subjectsModule,
		module {
			single<SubjectCatalogRepository> { FakeSubjectCatalogRepository() }
			single<ReportingRepository> { RecordingReportingRepository() }
			single<EventPublisher> { NoOpEventPublisher }
			single<TuIndiceDispatchers> { DefaultTuIndiceDispatchers }
		}
	) {
		assertResolves(SubjectSearchViewModel::class)
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

private class FakeSubjectCatalogRepository : SubjectCatalogRepository {
	override fun observeSearchResults(
		query: String,
		limit: Int
	): Flow<List<SubjectSearchResult>> = emptyFlow()

	override suspend fun refreshSearchResults(
		query: String,
		limit: Int
	) {
		error("This smoke test should not execute repository calls.")
	}
}
