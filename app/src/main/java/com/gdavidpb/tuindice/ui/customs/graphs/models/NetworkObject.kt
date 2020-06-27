package com.gdavidpb.tuindice.ui.customs.graphs.models

import android.graphics.Paint
import android.graphics.Path

abstract class NetworkObject(
        open val id: Int,
        open val path: Path,
        open val paint: Paint,
        open val isClickable: Boolean
) {
    override fun hashCode() = id

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Node

        if (id != other.id) return false

        return true
    }
}