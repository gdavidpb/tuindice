package com.gdavidpb.tuindice.ui.customs.graphs.models

import android.graphics.Paint
import android.graphics.Path

data class Node(
        override val id: Int,
        override val path: Path,
        override val paint: Paint
) : NetworkObject(id, path, paint, true)