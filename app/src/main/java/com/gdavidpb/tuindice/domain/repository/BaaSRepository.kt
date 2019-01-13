package com.gdavidpb.tuindice.domain.repository

import io.reactivex.Completable
import io.reactivex.Single

interface BaaSRepository {
    fun sendSignInLink(email: String): Completable
    fun signInWithLink(email: String, link: String): Completable
    fun signOut(): Completable
    fun isSignInLink(link: String): Single<Boolean>
}