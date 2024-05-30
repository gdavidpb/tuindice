package com.gdavidpb.tuindice.base.domain.exception

class HttpException(
	val code: Int,
	message: String,
	val response: String?
) : Throwable("HTTP $code $message")