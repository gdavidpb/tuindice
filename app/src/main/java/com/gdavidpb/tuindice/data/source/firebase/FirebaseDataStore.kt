package com.gdavidpb.tuindice.data.source.firebase

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.data.utils.URL_TU_INDICE
import com.gdavidpb.tuindice.data.utils.await
import com.gdavidpb.tuindice.domain.repository.BaaSRepository
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth

open class FirebaseDataStore(
        private val firebaseAuth: FirebaseAuth
) : BaaSRepository {
    override suspend fun isSignInLink(link: String): Boolean {
        return firebaseAuth.isSignInWithEmailLink(link)
    }

    override suspend fun sendSignInLink(email: String) {
        val actionCodeSettings = ActionCodeSettings
                .newBuilder()
                .setUrl(URL_TU_INDICE)
                .setHandleCodeInApp(true)
                .setAndroidPackageName(BuildConfig.APPLICATION_ID, true, null)
                .build()

        firebaseAuth.sendSignInLinkToEmail(email, actionCodeSettings).await()
    }

    override suspend fun signInWithLink(email: String, link: String) {
        firebaseAuth.signInWithEmailLink(email, link).await()
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }
}