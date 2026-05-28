package com.gdavidpb.tuindice.base.domain.usecase.base

interface ParamsValidator<P> {
	fun validate(params: P)
}