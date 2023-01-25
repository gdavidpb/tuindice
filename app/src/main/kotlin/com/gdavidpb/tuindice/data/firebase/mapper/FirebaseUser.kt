package com.gdavidpb.tuindice.data.firebase.mapper

import com.gdavidpb.tuindice.base.domain.model.Auth
import com.google.firebase.auth.FirebaseUser

fun FirebaseUser.toAuth() = Auth(
	uid = uid,
	email = email ?: error("email")
)