package com.gdavidpb.tuindice.domain.repository

import io.reactivex.Completable
import io.reactivex.Single

interface SettingsRepository {
    fun setFirstRun(): Completable
    fun setCooldown(key: String): Completable
    fun isCooldown(key: String): Single<Boolean>
    fun isFirstRun(): Single<Boolean>
    fun clear(): Completable
}