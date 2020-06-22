package com.gdavidpb.tuindice.domain.repository

interface ReportingRepository {
    fun setIdentifier(identifier: String)
    fun logException(throwable: Throwable)

    fun <T : Any> setCustomKey(key: String, value: T)
}