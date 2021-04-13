package com.gdavidpb.tuindice.data.source.firebase

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.domain.model.Auth
import com.gdavidpb.tuindice.domain.model.Credentials
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.utils.extensions.mode
import com.gdavidpb.tuindice.utils.mappers.asUsbEmail
import com.gdavidpb.tuindice.utils.mappers.toAuth
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

open class FirebaseDataSource(
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

    override suspend fun signUp(credentials: Credentials): Auth {
        val email = credentials.usbId.asUsbEmail()

        return auth.createUserWithEmailAndPassword(email, credentials.password)
                .await()
                .user
                ?.toAuth()
                ?: error("signUp")
    }

    override suspend fun signIn(credentials: Credentials): Auth {
        val email = credentials.usbId.asUsbEmail()

        return auth.signInWithEmailAndPassword(email, credentials.password)
                .await()
                .user
                ?.toAuth()
                ?: error("signIn")
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        val continueUrl = BuildConfig.URL_SIGN.format("test")

        val actionCodeSettings = ActionCodeSettings
                .newBuilder()
                .setUrl(continueUrl)
                .setHandleCodeInApp(true)
                .setAndroidPackageName(BuildConfig.APPLICATION_ID, true, null)
                .build()

        auth.sendPasswordResetEmail(email, actionCodeSettings).await()
    }

    override suspend fun confirmPasswordReset(code: String, password: String) {
        auth.confirmPasswordReset(code, password).await()
    }

    override suspend fun isResetPasswordLink(link: String): Boolean {
        return link.mode == "resetPassword"
    }
}