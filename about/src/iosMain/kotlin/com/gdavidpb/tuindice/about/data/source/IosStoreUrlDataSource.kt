package com.gdavidpb.tuindice.about.data.source

import com.gdavidpb.tuindice.about.data.repository.StoreUrlDataRepository

class IosStoreUrlDataSource : StoreUrlDataRepository {
	override fun getStoreUrl(): String {
		return "itms-apps://apps.apple.com/app/id6760307454"
	}
}
