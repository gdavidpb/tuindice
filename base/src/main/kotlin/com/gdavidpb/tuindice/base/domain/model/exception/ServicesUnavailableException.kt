package com.gdavidpb.tuindice.base.domain.model.exception

import com.gdavidpb.tuindice.base.domain.model.ServicesStatus

class ServicesUnavailableException(val servicesStatus: ServicesStatus) : IllegalStateException()