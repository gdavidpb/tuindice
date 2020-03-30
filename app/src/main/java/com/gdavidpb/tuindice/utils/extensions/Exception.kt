package com.gdavidpb.tuindice.utils.extensions

import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageException.ERROR_NOT_AUTHENTICATED
import com.google.firebase.storage.StorageException.ERROR_NOT_AUTHORIZED

fun Throwable.isPermissionDenied() = when (val internal = cause) {
    is FirebaseFirestoreException -> {
        internal.code == FirebaseFirestoreException.Code.UNAUTHENTICATED ||
                internal.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
    }
    is StorageException -> {
        internal.errorCode == ERROR_NOT_AUTHENTICATED ||
                internal.errorCode == ERROR_NOT_AUTHORIZED
    }
    else -> false
}