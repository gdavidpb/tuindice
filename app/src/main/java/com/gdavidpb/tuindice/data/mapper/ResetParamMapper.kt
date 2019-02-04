package com.gdavidpb.tuindice.data.mapper

import android.util.Base64
import com.gdavidpb.tuindice.domain.mapper.BidirectionalMapper

open class ResetParamMapper : BidirectionalMapper<String, Pair<String, String>> {
    override fun mapTo(from: String): Pair<String, String> {
        val data = String(Base64.decode(from, Base64.DEFAULT)).split("\n")

        return data.first() to data.last()
    }

    override fun mapFrom(to: Pair<String, String>): String {
        val data = "${to.first}\n${to.second}".toByteArray()

        return Base64.encodeToString(data, Base64.DEFAULT)
    }
}