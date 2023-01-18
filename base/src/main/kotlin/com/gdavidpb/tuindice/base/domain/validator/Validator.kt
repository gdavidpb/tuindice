package com.gdavidpb.tuindice.base.domain.validator

interface Validator<T> {
	fun validate(params: T)
}