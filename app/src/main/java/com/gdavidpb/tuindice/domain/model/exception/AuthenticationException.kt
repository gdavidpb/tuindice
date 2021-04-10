package com.gdavidpb.tuindice.domain.model.exception

import com.gdavidpb.tuindice.domain.model.AuthResponseCode

class AuthenticationException(val code: AuthResponseCode, message: String) : IllegalStateException(message)