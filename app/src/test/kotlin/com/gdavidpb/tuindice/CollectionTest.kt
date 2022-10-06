package com.gdavidpb.tuindice

import com.gdavidpb.tuindice.utils.extensions.component6
import com.gdavidpb.tuindice.utils.extensions.component7
import com.gdavidpb.tuindice.utils.extensions.component8
import org.junit.Assert.assertEquals
import org.junit.Test

class CollectionTest {
    @Test
    fun components678_Test() {
        val collection = (0..100).toList()
        val (_, _, _, _, _, six, seven, eight) = collection
        val expectedSix = collection.elementAt(5)
        val expectedSeven = collection.elementAt(6)
        val expectedEight = collection.elementAt(7)

        assertEquals(expectedSix, six)
        assertEquals(expectedSeven, seven)
        assertEquals(expectedEight, eight)
    }
}