package com.gdavidpb.tuindice.domain.repository

interface RemoteDatabaseRepository {
    suspend fun setToken()
}