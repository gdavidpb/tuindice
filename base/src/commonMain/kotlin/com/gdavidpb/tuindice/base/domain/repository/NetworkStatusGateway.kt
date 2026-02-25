package com.gdavidpb.tuindice.base.domain.repository

interface NetworkStatusGateway {
	fun isAvailable(): Boolean
}
