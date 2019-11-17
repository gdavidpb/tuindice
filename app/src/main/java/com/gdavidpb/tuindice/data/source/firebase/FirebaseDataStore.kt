package com.gdavidpb.tuindice.data.source.firebase

import android.net.Uri
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.domain.model.Auth
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.utils.await
import com.gdavidpb.tuindice.utils.fromResetParam
import com.gdavidpb.tuindice.utils.toAuth
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth

open class FirebaseDataStore(
        private val auth: FirebaseAuth
) : AuthRepository {
    override suspend fun getActiveAuth(): Auth? {
        return auth.currentUser?.toAuth()
    }

    override suspend fun signUp(email: String, password: String): Auth {
        return auth.createUserWithEmailAndPassword(email, password).await().user!!.toAuth()
    }

    override suspend fun signIn(email: String, password: String): Auth {
        return auth.signInWithEmailAndPassword(email, password).await().user!!.toAuth()
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun sendPasswordResetEmail(email: String, password: String) {
        val resetPassword = (email to password).fromResetParam()
        val continueUrl = BuildConfig.URL_APP_RESET_PASSWORD.format(resetPassword)

        val actionCodeSettings = ActionCodeSettings
                .newBuilder()
                .setUrl(continueUrl)
                .setHandleCodeInApp(true)
                .setAndroidPackageName(BuildConfig.APPLICATION_ID, true, null)
                .build()

        auth.sendPasswordResetEmail(email, actionCodeSettings).await()
    }

    override suspend fun sendEmailVerification() {
        auth.currentUser?.also { user ->
            val continueUrl = BuildConfig.URL_APP_VERIFY.format(user.uid)

            val actionCodeSettings = ActionCodeSettings
                    .newBuilder()
                    .setUrl(continueUrl)
                    .setHandleCodeInApp(false)
                    .setAndroidPackageName(BuildConfig.APPLICATION_ID, true, null)
                    .build()

            user.sendEmailVerification(actionCodeSettings).await()
        }
    }

    override suspend fun confirmPasswordReset(code: String, password: String) {
        auth.confirmPasswordReset(code, password).await()
    }

    override suspend fun isEmailVerified(): Boolean {
        val lazyIsEmailVerified = { auth.currentUser?.isEmailVerified == true }

        return lazyIsEmailVerified() || auth.currentUser?.reload()?.await().run { lazyIsEmailVerified() }
    }

    override suspend fun isResetLink(link: String?): Boolean {
        if (link == null) return false

        val uri = Uri.parse(link)

        val values = fun(uri: Uri) = mapOf(
                uri.host to BuildConfig.URL_APP_BASE,
                uri.path to "path=/__/auth/onClick",
                uri.getQueryParameter("mode") to "resetPassword"
        )

        return values(uri).all { (value, expected) -> value == expected }
    }
}