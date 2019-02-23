package com.gdavidpb.tuindice.data.source.firestore

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.Record
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

open class FirestoreDataStore(
        private val auth: FirebaseAuth,
        private val firestore: FirebaseFirestore
) : DatabaseRepository {
    override suspend fun updateAccount(account: Account) {
        val uid = auth.uid ?: return

        val userRef = firestore.collection(COLLECTION_USER).document(uid)

        val values = account.run {
            mapOf(
                    FIELD_USER_ID to account.id,
                    FIELD_USER_USB_ID to account.usbId,
                    FIELD_USER_TOKEN to account.token,
                    FIELD_USER_EMAIL to account.email,
                    FIELD_USER_FULL_NAME to account.fullName,
                    FIELD_USER_FIRST_NAMES to account.firstNames,
                    FIELD_USER_LAST_NAMES to account.lastNames,
                    FIELD_USER_SCHOLARSHIP to account.scholarship,
                    FIELD_USER_CAREER_NAME to account.careerName,
                    FIELD_USER_CAREER_CODE to account.careerCode

            )
        }.filter { (_, value) ->
            when (value) {
                is String -> value.isNotEmpty()
                else -> true
            }
        }

        userRef.set(values, SetOptions.merge()).await()
    }

    override suspend fun updateRecord(record: Record) {

    }
}