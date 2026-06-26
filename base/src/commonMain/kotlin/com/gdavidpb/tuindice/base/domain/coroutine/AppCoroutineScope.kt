package com.gdavidpb.tuindice.base.domain.coroutine

import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class AppCoroutineScope(
	dispatchers: TuIndiceDispatchers
) : CoroutineScope {
	private val scope = CoroutineScope(SupervisorJob() + dispatchers.default)

	override val coroutineContext: CoroutineContext
		get() = scope.coroutineContext
}
