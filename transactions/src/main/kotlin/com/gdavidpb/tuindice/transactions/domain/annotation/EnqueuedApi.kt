package com.gdavidpb.tuindice.transactions.domain.annotation

import com.gdavidpb.tuindice.transactions.data.repository.transactions.source.api.retrofit.TransactionParser
import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
annotation class EnqueuedApi(
	val parser: KClass<out TransactionParser>
)