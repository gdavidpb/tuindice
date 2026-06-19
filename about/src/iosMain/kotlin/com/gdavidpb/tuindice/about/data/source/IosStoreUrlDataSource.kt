package com.gdavidpb.tuindice.about.data.source

import com.gdavidpb.tuindice.about.domain.repository.StoreUrlRepository

class IosStoreUrlDataSource(
	private val appStoreUrl: String
) : StoreUrlRepository {
	override fun getStoreUrl(): String {
		return appStoreUrl
	}
}
