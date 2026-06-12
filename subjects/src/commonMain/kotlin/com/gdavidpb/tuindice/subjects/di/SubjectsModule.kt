package com.gdavidpb.tuindice.subjects.di

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicPensumStatusEngine
import com.gdavidpb.tuindice.subjects.data.repository.SubjectCatalogLocalDataRepository
import com.gdavidpb.tuindice.subjects.data.repository.SubjectCatalogRemoteDataRepository
import com.gdavidpb.tuindice.subjects.data.repository.SubjectSearchPensumStatusDataRepository
import com.gdavidpb.tuindice.subjects.data.repository.SubjectStatsApiDataRepository
import com.gdavidpb.tuindice.subjects.data.repository.SubjectStatsLocalDataRepository
import com.gdavidpb.tuindice.subjects.data.source.SubjectCatalogDataSource
import com.gdavidpb.tuindice.subjects.data.source.SubjectCatalogRoomDataSource
import com.gdavidpb.tuindice.subjects.data.source.KtorSubjectsApiDataSource
import com.gdavidpb.tuindice.subjects.data.source.SubjectSearchPensumStatusDataSource
import com.gdavidpb.tuindice.subjects.data.source.SubjectStatsDataSource
import com.gdavidpb.tuindice.subjects.data.source.SubjectStatsRoomDataSource
import com.gdavidpb.tuindice.subjects.domain.repository.SubjectCatalogRepository
import com.gdavidpb.tuindice.subjects.domain.repository.SubjectStatsRepository
import com.gdavidpb.tuindice.subjects.domain.usecase.LoadSubjectDetailUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.ObserveSubjectSearchUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.RefreshSubjectDetailUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.RefreshSubjectSearchUseCase
import com.gdavidpb.tuindice.subjects.presentation.machine.SubjectDetailMachine
import com.gdavidpb.tuindice.subjects.presentation.machine.SubjectSearchDraft
import com.gdavidpb.tuindice.subjects.presentation.machine.SubjectSearchMachine
import com.gdavidpb.tuindice.subjects.presentation.viewmodel.SubjectDetailViewModel
import com.gdavidpb.tuindice.subjects.presentation.viewmodel.SubjectSearchViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val subjectsModule = module {
	viewModelOf(::SubjectDetailViewModel)
	viewModelOf(::SubjectSearchViewModel)

	factoryOf(::SubjectDetailMachine)
	factoryOf(::SubjectSearchMachine)
	factoryOf(::SubjectSearchDraft)

	factoryOf(::LoadSubjectDetailUseCase)
	factoryOf(::RefreshSubjectDetailUseCase)
	factoryOf(::ObserveSubjectSearchUseCase)
	factoryOf(::RefreshSubjectSearchUseCase)

	singleOf(::KtorSubjectsApiDataSource)
	singleOf(::AcademicPensumStatusEngine)
	single<SubjectStatsApiDataRepository> { get<KtorSubjectsApiDataSource>() }
	single<SubjectCatalogRemoteDataRepository> { get<KtorSubjectsApiDataSource>() }
	singleOf(::SubjectSearchPensumStatusDataSource) { bind<SubjectSearchPensumStatusDataRepository>() }
	singleOf(::SubjectCatalogRoomDataSource) { bind<SubjectCatalogLocalDataRepository>() }
	singleOf(::SubjectStatsRoomDataSource) { bind<SubjectStatsLocalDataRepository>() }
	singleOf(::SubjectStatsDataSource) { bind<SubjectStatsRepository>() }
	singleOf(::SubjectCatalogDataSource) { bind<SubjectCatalogRepository>() }
}
