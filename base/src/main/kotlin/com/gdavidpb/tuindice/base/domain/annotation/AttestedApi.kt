package com.gdavidpb.tuindice.base.domain.annotation

import com.gdavidpb.tuindice.base.data.repository.source.api.retrofit.AttestationParser
import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
annotation class AttestedApi(
	val parser: KClass<out AttestationParser<*>>
)