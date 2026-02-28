package com.gdavidpb.tuindice.about.data.source

import com.gdavidpb.tuindice.about.data.repository.StoreUrlDataSource

class IosStoreUrlDataSource : StoreUrlDataSource {
	override fun getStoreUrl(): String {
		return "itms-apps://apps.apple.com"
	}
}
