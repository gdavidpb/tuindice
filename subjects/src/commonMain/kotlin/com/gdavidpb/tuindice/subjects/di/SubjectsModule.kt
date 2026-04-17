package com.gdavidpb.tuindice.subjects.di

import com.gdavidpb.tuindice.subjects.data.repository.SubjectStatsApiDataRepository
import com.gdavidpb.tuindice.subjects.data.repository.SubjectStatsLocalDataRepository
import com.gdavidpb.tuindice.subjects.data.source.KtorSubjectsApiDataSource
import com.gdavidpb.tuindice.subjects.data.source.SubjectStatsDataSource
import com.gdavidpb.tuindice.subjects.data.source.SubjectStatsRoomDataSource
import com.gdavidpb.tuindice.subjects.domain.repository.SubjectStatsRepository
import com.gdavidpb.tuindice.subjects.domain.usecase.LoadSubjectDetailUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.RefreshSubjectDetailUseCase
import com.gdavidpb.tuindice.subjects.presentation.action.LoadSubjectDetailActionProcessor
import com.gdavidpb.tuindice.subjects.presentation.action.RefreshSubjectDetailActionProcessor
import com.gdavidpb.tuindice.subjects.presentation.action.SelectSubjectSegmentTabActionProcessor
import com.gdavidpb.tuindice.subjects.presentation.viewmodel.SubjectDetailViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val subjectsModule = module {
	viewModelOf(::SubjectDetailViewModel)

	factoryOf(::LoadSubjectDetailActionProcessor)
	factoryOf(::RefreshSubjectDetailActionProcessor)
	factoryOf(::SelectSubjectSegmentTabActionProcessor)

	factoryOf(::LoadSubjectDetailUseCase)
	factoryOf(::RefreshSubjectDetailUseCase)

	singleOf(::KtorSubjectsApiDataSource)
	single<SubjectStatsApiDataRepository> { get<KtorSubjectsApiDataSource>() }
	singleOf(::SubjectStatsRoomDataSource) { bind<SubjectStatsLocalDataRepository>() }
	singleOf(::SubjectStatsDataSource) { bind<SubjectStatsRepository>() }
}
