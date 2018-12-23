package com.gdavidpb.tuindice

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.standalone.StandAloneContext.stopKoin
import org.koin.test.KoinTest

class ExampleInstrumentedTest : KoinTest {
    @Before
    fun before() {
    }

    @Test
    fun koinTest() {

    }

    @After
    fun after() {
        stopKoin()
    }
}
