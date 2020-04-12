package com.gdavidpb.tuindice.utils.extensions

import com.gdavidpb.tuindice.domain.model.AuthResponseCode
import com.gdavidpb.tuindice.domain.model.exception.AuthenticationException
import com.gdavidpb.tuindice.domain.model.exception.SynchronizationException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageException.ERROR_NOT_AUTHENTICATED
import com.google.firebase.storage.StorageException.ERROR_NOT_AUTHORIZED
import retrofit2.HttpException
import java.io.IOException

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

fun Throwable.isInvalidCredentials() =
        ((this as? AuthenticationException)?.code) == AuthResponseCode.INVALID_CREDENTIALS

fun Throwable.isNoNetworkAvailable(isNetworkAvailable: Boolean) = when {
    this is IOException && !isNetworkAvailable -> true
    this is HttpException && !isNetworkAvailable -> true
    else -> false
}

fun Throwable.isConnectionIssue() = when (this) {
    is IOException, is HttpException -> true
    else -> false
}

fun Throwable.isSynchronizationIssue() = this is SynchronizationException