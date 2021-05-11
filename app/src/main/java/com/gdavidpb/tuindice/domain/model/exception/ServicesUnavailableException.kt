package com.gdavidpb.tuindice.domain.model.exception

import com.gdavidpb.tuindice.domain.model.ServicesStatus

class ServicesUnavailableException(val servicesStatus: ServicesStatus) : IllegalStateException()