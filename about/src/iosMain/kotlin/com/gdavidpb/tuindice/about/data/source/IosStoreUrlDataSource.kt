package com.gdavidpb.tuindice.about.data.source

import com.gdavidpb.tuindice.about.data.contract.StoreUrlDataSource

class IosStoreUrlDataSource : StoreUrlDataSource {
	override fun getStoreUrl(): String {
		return "itms-apps://apps.apple.com/app/id6760307454"
	}
}
