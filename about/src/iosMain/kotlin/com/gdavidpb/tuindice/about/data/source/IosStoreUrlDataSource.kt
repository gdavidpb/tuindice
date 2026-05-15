package com.gdavidpb.tuindice.about.data.source

import com.gdavidpb.tuindice.about.domain.repository.StoreUrlRepository

class IosStoreUrlDataSource : StoreUrlRepository {
	override fun getStoreUrl(): String {
		return "itms-apps://apps.apple.com/app/id6760307454"
	}
}
