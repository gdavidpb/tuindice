package com.gdavidpb.tuindice.domain.repository

interface LinkRepository {
    suspend fun resolveLink(data: String): String
}