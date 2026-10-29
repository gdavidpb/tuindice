package com.gdavidpb.tuindice.base.data.source

import com.gdavidpb.tuindice.base.data.repository.config.RemoteConfigDataRepository
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantMode
import com.gdavidpb.tuindice.base.domain.repository.AssistantConfigRepository
import com.gdavidpb.tuindice.base.utils.DefaultRemoteConfigValues
import com.gdavidpb.tuindice.base.utils.RemoteConfigKeys
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

class AssistantConfigDataSource(
	private val remoteConfigDataSource: RemoteConfigDataRepository,
	private val defaults: DefaultRemoteConfigValues
) : AssistantConfigRepository {
	override fun observeAssistantMode(): Flow<AssistantMode> {
		return channelFlow {
			// Subscribe before the first read, so an update that lands between the two is not lost.
			launch(start = CoroutineStart.UNDISPATCHED) {
				remoteConfigDataSource.observeUpdatedKeys()
					.filter { RemoteConfigKeys.ASSISTANT_MODE in it }
					.collect { send(readAssistantMode()) }
			}

			send(readAssistantMode())
		}.distinctUntilChanged()
	}

	// A missing value takes the default; an invalid one fails closed through fromRemoteValue.
	private fun readAssistantMode(): AssistantMode {
		return AssistantMode.fromRemoteValue(
			remoteConfigDataSource.getString(RemoteConfigKeys.ASSISTANT_MODE) ?: defaults.assistantMode
		)
	}
}
