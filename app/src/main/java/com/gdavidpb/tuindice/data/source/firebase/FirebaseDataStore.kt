package com.gdavidpb.tuindice.data.source.firebase

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.data.utils.URL_TU_INDICE
import com.gdavidpb.tuindice.domain.repository.BaaSRepository
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.Completable
import io.reactivex.Single

open class FirebaseDataStore(
        private val firebaseAuth: FirebaseAuth
) : BaaSRepository {
    override fun isSignInLink(link: String): Single<Boolean> {
        return Single.just(firebaseAuth.isSignInWithEmailLink(link))
    }

    override fun sendSignInLink(email: String): Completable {
        return Completable.create { emitter ->
            val actionCodeSettings = ActionCodeSettings
                    .newBuilder()
                    .setUrl(URL_TU_INDICE)
                    .setHandleCodeInApp(true)
                    .setAndroidPackageName(BuildConfig.APPLICATION_ID, true, null)
                    .build()

            firebaseAuth.sendSignInLinkToEmail(email, actionCodeSettings)
                    .addOnCompleteListener {
                        emitter.onComplete()
                    }.addOnFailureListener {
                        emitter.onError(it)
                    }
        }
    }

    override fun signInWithLink(email: String, link: String): Completable {
        return Completable.create { emitter ->
            firebaseAuth.signInWithEmailLink(email, link)
                    .addOnCompleteListener {
                        emitter.onComplete()
                    }.addOnFailureListener {
                        emitter.onError(it)
                    }
        }
    }

    override fun signOut(): Completable {
        return Completable.fromCallable(firebaseAuth::signOut)
    }
}