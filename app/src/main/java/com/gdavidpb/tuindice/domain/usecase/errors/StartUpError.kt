package com.gdavidpb.tuindice.domain.usecase.errors

import com.gdavidpb.tuindice.domain.model.ServicesStatus

sealed class StartUpError {
    object InvalidLink : StartUpError()
    class NoServices(val servicesStatus: ServicesStatus) : StartUpError()
    class NoConnection(val isNetworkAvailable: Boolean) : StartUpError()
}
