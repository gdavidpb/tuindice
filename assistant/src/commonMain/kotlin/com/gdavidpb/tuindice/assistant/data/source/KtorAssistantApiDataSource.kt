package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.repository.AssistantServerDisabledDataRepository
import io.ktor.client.HttpClient

// The assistant API the data layer calls. Each row of the contract is a function beside this class, grouped by
// resource in the Assistant*Api files, so the class stays small and every function keeps its contract path and JSON.
class KtorAssistantApiDataSource(
	internal val httpClient: HttpClient,
	internal val serverDisabled: AssistantServerDisabledDataRepository
)
