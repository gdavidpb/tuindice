package com.gdavidpb.tuindice.base.domain.usecase.base

interface ParamsValidator<T> {
	fun validate(params: T)
}