package com.gdavidpb.tuindice.data.model.exception

import com.gdavidpb.tuindice.domain.model.AuthResponseCode

open class AuthException(val code: AuthResponseCode, message: String) : SecurityException(message)