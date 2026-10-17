package com.gdavidpb.tuindice.assistant.di

import com.gdavidpb.tuindice.assistant.data.repository.AssistantEventsDataRepository
import com.gdavidpb.tuindice.assistant.data.repository.AssistantServerDisabledDataRepository
import com.gdavidpb.tuindice.assistant.data.source.AssistantAvailabilityDataSource
import com.gdavidpb.tuindice.assistant.data.source.AssistantConsentSettingsDataSource
import com.gdavidpb.tuindice.assistant.data.source.AssistantConversationDataSource
import com.gdavidpb.tuindice.assistant.data.source.AssistantInsightsDataSource
import com.gdavidpb.tuindice.assistant.data.source.AssistantMemoryDataSource
import com.gdavidpb.tuindice.assistant.data.source.KtorAssistantApiDataSource
import com.gdavidpb.tuindice.assistant.data.source.SseAssistantEventsDataSource
import com.gdavidpb.tuindice.base.data.source.network.ASSISTANT_STREAM_HTTP_CLIENT_QUALIFIER
import com.gdavidpb.tuindice.base.domain.repository.AssistantAvailabilityRepository
import com.gdavidpb.tuindice.base.domain.repository.AssistantConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.AssistantInsightsRepository
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val assistantModule = module {
	/* Data sources */

	factoryOf(::KtorAssistantApiDataSource)
	single<AssistantEventsDataRepository> {
		SseAssistantEventsDataSource(
			httpClient = get<HttpClient>(named(ASSISTANT_STREAM_HTTP_CLIENT_QUALIFIER)),
			serverDisabled = get()
		)
	}
	singleOf(::AssistantAvailabilityDataSource) {
		bind<AssistantAvailabilityRepository>()
		bind<AssistantServerDisabledDataRepository>()
		bind<SessionMemory>()
	}
	singleOf(::AssistantConsentSettingsDataSource) {
		bind<AssistantConsentRepository>()
		bind<SessionMemory>()
	}
	singleOf(::AssistantInsightsDataSource) {
		bind<AssistantInsightsRepository>()
		bind<SessionMemory>()
	}
	singleOf(::AssistantMemoryDataSource) { bind<SessionMemory>() }
	singleOf(::AssistantConversationDataSource) { bind<SessionMemory>() }
}
