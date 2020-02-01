package com.gdavidpb.tuindice.domain.usecase.coroutines

import androidx.lifecycle.MutableLiveData
import com.gdavidpb.tuindice.domain.repository.ReportingRepository
import kotlinx.coroutines.CoroutineScope
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.coroutines.CoroutineContext

abstract class BaseUseCase<T, Q : MutableLiveData<*>>(
        protected open val backgroundContext: CoroutineContext,
        protected open val foregroundContext: CoroutineContext
) : KoinComponent {

    val reportingRepository by inject<ReportingRepository>()

    abstract fun execute(params: T, liveData: Q, coroutineScope: CoroutineScope)
}