package com.gdavidpb.tuindice.login.domain.error

import com.gdavidpb.tuindice.base.domain.model.ServicesStatus

sealed class StartUpError {
    class NoServices(val servicesStatus: ServicesStatus) : StartUpError()
    class NoConnection(val isNetworkAvailable: Boolean) : StartUpError()
}