package com.gdavidpb.tuindice.domain.repository

interface ReportingRepository {
    fun setIdentifier(identifier: String)
    fun logException(throwable: Throwable)

    fun setInt(key: String, value: Int)
    fun setLong(key: String, value: Long)
    fun setString(key: String, value: String)
}