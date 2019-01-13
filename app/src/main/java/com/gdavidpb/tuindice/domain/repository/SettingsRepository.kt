package com.gdavidpb.tuindice.domain.repository

import io.reactivex.Completable
import io.reactivex.Single

interface SettingsRepository {
    fun getEmailSentTo(): Single<String>
    fun setEmailSentTo(email: String): Completable
    fun clearEmailSentTo(): Completable
    fun setFirstRun(): Completable
    fun setCooldown(key: String): Completable
    fun isCooldown(key: String): Single<Boolean>
    fun isFirstRun(): Single<Boolean>
    fun clear(): Completable
}