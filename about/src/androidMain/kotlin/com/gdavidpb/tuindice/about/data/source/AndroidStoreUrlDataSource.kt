package com.gdavidpb.tuindice.about.data.source

import android.content.Context
import com.gdavidpb.tuindice.about.data.source.StoreUrlDataSource

class AndroidStoreUrlDataSource(
	private val context: Context
) : StoreUrlDataSource {
	override fun getStoreUrl(): String {
		return "market://details?id=${context.packageName}"
	}
}
