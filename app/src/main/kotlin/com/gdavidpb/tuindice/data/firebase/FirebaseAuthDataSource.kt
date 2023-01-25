package com.gdavidpb.tuindice.data.firebase

import com.gdavidpb.tuindice.base.domain.model.Auth
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.utils.extension.awaitOrNull
import com.gdavidpb.tuindice.data.firebase.mapper.toAuth
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class FirebaseAuthDataSource(
	private val auth: FirebaseAuth
) : AuthRepository {
	override suspend fun isActiveAuth(): Boolean {
		return auth.currentUser != null
	}

	override suspend fun getActiveAuth(): Auth {
		return auth.currentUser
			?.toAuth()
			?: error("getActiveAuth")
	}

	override suspend fun getActiveToken(): String {
		return auth.currentUser
			?.getIdToken(true)
			?.awaitOrNull()
			?.token
			?: error("getActiveToken")
	}

	override suspend fun signIn(token: String): Auth {
		return auth.signInWithCustomToken(token)
			.await()
			.user
			?.toAuth()
			?: error("signIn")
	}

	override suspend fun signOut() {
		auth.signOut()
	}
}