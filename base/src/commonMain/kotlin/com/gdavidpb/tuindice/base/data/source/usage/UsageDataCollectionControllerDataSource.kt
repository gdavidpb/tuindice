package com.gdavidpb.tuindice.base.data.source.usage

import com.gdavidpb.tuindice.base.domain.controller.UsageDataCollectionController
import com.gdavidpb.tuindice.base.domain.repository.UsageDataConsentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.launch

class UsageDataCollectionControllerDataSource(
	private val usageDataConsentRepository: UsageDataConsentRepository,
	private val setCollectionEnabled: (Boolean) -> Unit,
	private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : UsageDataCollectionController {
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
}
