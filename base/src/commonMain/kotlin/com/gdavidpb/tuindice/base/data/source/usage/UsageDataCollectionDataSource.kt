package com.gdavidpb.tuindice.base.data.source.usage

import com.gdavidpb.tuindice.base.domain.repository.UsageDataConsentRepository
import com.gdavidpb.tuindice.base.domain.startup.AppStartupTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.launch

class UsageDataCollectionDataSource(
	private val usageDataConsentRepository: UsageDataConsentRepository,
	private val setCollectionEnabledActions: List<(Boolean) -> Unit>,
	private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : AppStartupTask {
	private var started = false

	override fun start() {
		if (started) return

		started = true
		val initialValue = usageDataConsentRepository.isUsageDataCollectionEnabled()
		setCollectionEnabled(initialValue)
		scope.launch {
			usageDataConsentRepository.usageDataCollectionEnabled
				.dropWhile { enabled -> enabled == initialValue }
				.collectLatest { enabled ->
					setCollectionEnabled(enabled)
				}
		}
	}

	private fun setCollectionEnabled(enabled: Boolean) {
		setCollectionEnabledActions.forEach { action ->
			action(enabled)
		}
	}
}
