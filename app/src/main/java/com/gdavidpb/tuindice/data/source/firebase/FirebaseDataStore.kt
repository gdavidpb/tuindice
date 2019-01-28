package com.gdavidpb.tuindice.data.source.firebase

import android.net.Uri
import android.util.Base64
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.data.mapper.AuthResultMapper
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.utils.await
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth

open class FirebaseDataStore(
        private val firebaseAuth: FirebaseAuth,
        private val authResultMapper: AuthResultMapper
) : AuthRepository {
    override suspend fun signUp(email: String, password: String): Account {
        return firebaseAuth.createUserWithEmailAndPassword(email, password).await().let(authResultMapper::map)
    }

    override suspend fun signIn(email: String, password: String): Account {
        return firebaseAuth.signInWithEmailAndPassword(email, password).await().let(authResultMapper::map)
    }

    override suspend fun confirmPasswordReset(code: String, password: String) {
        firebaseAuth.confirmPasswordReset(code, password).await()
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun sendPasswordResetEmail(email: String, password: String) {
        val actionCodeSettings = ActionCodeSettings
                .newBuilder()
                .setUrl(Base64.encodeToString(password.toByteArray(), Base64.DEFAULT))
                .setHandleCodeInApp(true)
                .setAndroidPackageName(BuildConfig.APPLICATION_ID, true, null)
                .build()

        firebaseAuth.sendPasswordResetEmail(email, actionCodeSettings).await()
    }

    override suspend fun sendEmailVerification() {
        val actionCodeSettings = ActionCodeSettings
                .newBuilder()
                .setHandleCodeInApp(true)
                .setAndroidPackageName(BuildConfig.APPLICATION_ID, true, null)
                .build()

        firebaseAuth.currentUser?.sendEmailVerification(actionCodeSettings)
    }

    override suspend fun isEmailVerified(): Boolean {
        return firebaseAuth.currentUser?.isEmailVerified == true
    }

    override suspend fun isSignedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override suspend fun isResetLink(link: String?): Boolean {
        if (link.isNullOrEmpty()) return false

        val uri = Uri.parse(link)

        val values = fun(uri: Uri): Map<String, String> {
            return mapOf(
                    uri.host to "tuindice-usb.firebaseapp.com",
                    uri.path to "path=/__/auth/action",
                    uri.getQueryParameter("mode") to "resetPassword"
            )
        }

        return values(uri).all { (value, expected) -> value == expected }
    }

    override suspend fun isVerifyLink(link: String?): Boolean {
        if (link.isNullOrEmpty()) return false

        val uri = Uri.parse(link)

        val values = fun(uri: Uri): Map<String, String> {
            return mapOf(
                    uri.host to "tuindice-usb.firebaseapp.com",
                    uri.path to "path=/__/auth/action",
                    uri.getQueryParameter("mode") to "verifyEmail"
            )
        }

        return values(uri).all { (value, expected) -> value == expected }
    }
}