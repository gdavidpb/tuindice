package com.gdavidpb.tuindice.login.domain.repository

interface LoginRepository {
	suspend fun signIn(usbId: String, password: String)
	suspend fun signOut()
}