package com.gdavidpb.tuindice.data.source.firebase

import android.content.res.Resources
import android.net.Uri
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.mapper.ResetParamMapper
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.utils.*
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId

open class FirebaseDataStore(
        private val resources: Resources,
        private val auth: FirebaseAuth,
        private val firestore: FirebaseFirestore,
        private val instanceId: FirebaseInstanceId,
        private val settingsRepository: SettingsRepository,
        private val resetParamMapper: ResetParamMapper
) : AuthRepository {
    override suspend fun getActiveAccount(): Account? {
        return auth.currentUser?.run {
            getUserById(uid)
        }
    }

    override suspend fun signUp(email: String, password: String): Account {
        return auth.createUserWithEmailAndPassword(email, password).await().user.run {

            val token = instanceId.instanceId.await().token

            val values = mapOf(
                    FIELD_USER_EMAIL to email,
                    FIELD_USER_TOKEN to token
            )

            firestore.collection(COLLECTION_USER).document(uid).set(values)

            getUserById(uid)
        }
    }

    override suspend fun signIn(email: String, password: String): Account {
        return auth.signInWithEmailAndPassword(email, password).await().user.run {
            getUserById(uid)
        }
    }

    override suspend fun confirmPasswordReset(code: String, password: String) {
        auth.confirmPasswordReset(code, password).await()
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun sendPasswordResetEmail(email: String, password: String) {
        val resetPassword = (email to password).let(resetParamMapper::mapFrom)
        val continueUrl = resources.getString(R.string.urlContinueResetPassword, resetPassword)

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
            val continueUrl = resources.getString(R.string.urlContinueVerification, user.uid)

            settingsRepository.setIsAwaitingForVerify(user.email!!)

            val actionCodeSettings = ActionCodeSettings
                    .newBuilder()
                    .setUrl(continueUrl)
                    .setHandleCodeInApp(false)
                    .setAndroidPackageName(BuildConfig.APPLICATION_ID, true, null)
                    .build()

            user.sendEmailVerification(actionCodeSettings).await()
        }
    }

    override suspend fun isEmailVerified(): Boolean {
        return auth.currentUser?.isEmailVerified == true
    }

    override suspend fun isResetLink(link: String?): Boolean {
        if (link == null) return false

        val uri = Uri.parse(link)

        val values = fun(uri: Uri): Map<String, String> {
            return mapOf(
                    uri.host to URL_BASE,
                    uri.path to "path=/__/auth/action",
                    uri.getQueryParameter("mode") to "resetPassword"
            )
        }

        return values(uri).all { (value, expected) -> value == expected }
    }

    override suspend fun isVerifyLink(link: String?): Boolean {
        if (link == null) return false

        val uid = auth.uid ?: return false

        return link == resources.getString(R.string.urlContinueVerification, uid)
    }

    private suspend fun getUserById(uid: String): Account {
        return firestore
                .collection(COLLECTION_USER)
                .document(uid)
                .get()
                .await()
                .toObject(Account::class.java)!!
    }
}