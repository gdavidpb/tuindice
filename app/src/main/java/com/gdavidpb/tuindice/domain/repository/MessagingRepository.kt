package com.gdavidpb.tuindice.domain.repository

interface MessagingRepository {
    suspend fun getToken(): String?
}