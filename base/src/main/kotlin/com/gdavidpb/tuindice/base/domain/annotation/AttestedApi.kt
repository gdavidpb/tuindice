package com.gdavidpb.tuindice.base.domain.annotation

import com.gdavidpb.tuindice.base.domain.model.attestation.Attestation
import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
annotation class AttestedApi(
	val parser: KClass<out Attestation<*>>
)