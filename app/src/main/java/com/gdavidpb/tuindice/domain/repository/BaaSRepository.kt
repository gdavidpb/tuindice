package com.gdavidpb.tuindice.domain.repository

interface BaaSRepository {
    suspend fun sendSignInLink(email: String)
    suspend fun signInWithLink(email: String, link: String)
    suspend fun signOut()
    suspend fun isSignInLink(link: String): Boolean
}