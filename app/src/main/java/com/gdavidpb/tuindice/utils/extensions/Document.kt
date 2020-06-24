package com.gdavidpb.tuindice.utils.extensions

import org.jsoup.nodes.Node

private const val nodeNameComment = "#comment"
private const val nodeNameStyle = "style"

fun Node.removeCommentsAndStyles(): Node {
    var i = 0

    lateinit var child: Node
    lateinit var nodeName: String

    while (i < childNodeSize()) {
        child = childNode(i)
        nodeName = child.nodeName()

        if (nodeName == nodeNameComment || nodeName == nodeNameStyle) {
            child.remove()
        } else {
            child.removeCommentsAndStyles()
            i++
        }
    }

    return this
}