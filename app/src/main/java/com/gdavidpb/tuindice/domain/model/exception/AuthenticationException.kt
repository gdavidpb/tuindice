package com.gdavidpb.tuindice.domain.model.exception

import com.gdavidpb.tuindice.domain.model.AuthErrorCode

class AuthenticationException(val errorCode: AuthErrorCode, message: String) : IllegalStateException(message)