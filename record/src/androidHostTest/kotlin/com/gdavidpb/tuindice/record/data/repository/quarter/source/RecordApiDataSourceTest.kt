package com.gdavidpb.tuindice.record.data.repository.quarter.source

import com.gdavidpb.tuindice.record.RecordFixtures
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteQuarter
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class RecordApiDataSourceTest {
    private lateinit var wireMockServer: WireMockServer
    private lateinit var ktorClient: HttpClient
    private lateinit var dataSource: RecordApiDataSource

    @Before
    fun setUp() {
        wireMockServer = WireMockServer(0).also { it.start() }
        configureFor("localhost", wireMockServer.port())

        ktorClient = HttpClient(CIO) {
            expectSuccess = true

            install(DefaultRequest) {
                url(wireMockServer.baseUrl())
                contentType(ContentType.Application.Json)
            }

            install(ContentNegotiation) {
                json(
                    Json {
                        explicitNulls = true
                        prettyPrint = false
                    }
                )
            }
        }
        dataSource = RecordApiDataSource(ktorClient = ktorClient)
    }

    @After
    fun tearDown() {
        ktorClient.close()
        wireMockServer.stop()
    }

    @Test
    fun getQuarters_returnsMappedRemoteQuarters() = runBlocking {
        wireMockServer.stubFor(
            get(urlEqualTo("/quarters")).willReturn(
                okJson(
                    """
                    [
                      {
                        "id": "q1",
                        "name": "Enero - Marzo 2024",
                        "start_date": 1704067200000,
                        "end_date": 1711843200000,
                        "grade": 4.5,
                        "grade_sum": 4.2,
                        "credits": 6,
                        "credits_sum": 12,
                        "is_current": false,
                        "is_read_only": false,
                        "subjects": [
                          {
                            "id": "s1",
                            "qid": "q1",
                            "code": "MA1111",
                            "name": "MATEMATICAS I",
                            "credits": 4,
                            "grade": 5
                          }
                        ]
                      }
                    ]
                    """.trimIndent()
                )
            )
        )

        val result = dataSource.getQuarters()

        assertEquals(
            listOf(
                RecordFixtures.remoteQuarter(
                    id = "q1",
                    subjects = listOf(RecordFixtures.remoteSubject(id = "s1", quarterId = "q1"))
                )
            ),
            result
        )
    }

    @Test
    fun getQuarter_returnsMappedRemoteQuarter() = runBlocking {
        wireMockServer.stubFor(
            get(urlEqualTo("/quarters/q1")).willReturn(
                okJson(
                    """
                    {
                      "id": "q1",
                      "name": "Enero - Marzo 2024",
                      "start_date": 1704067200000,
                      "end_date": 1711843200000,
                      "grade": 4.5,
                      "grade_sum": 4.2,
                      "credits": 6,
                      "credits_sum": 12,
                      "is_current": false,
                      "is_read_only": false,
                      "subjects": [
                        {
                          "id": "s1",
                          "qid": "q1",
                          "code": "MA1111",
                          "name": "MATEMATICAS I",
                          "credits": 4,
                          "grade": 5
                        }
                      ]
                    }
                    """.trimIndent()
                )
            )
        )

        val result = dataSource.getQuarter("q1")

        assertEquals(
            RecordFixtures.remoteQuarter(
                id = "q1",
                subjects = listOf(RecordFixtures.remoteSubject(id = "s1", quarterId = "q1"))
            ),
            result
        )
    }

    @Test
    fun removeQuarter_issuesDeleteRequest() = runBlocking {
        wireMockServer.stubFor(
            delete(urlEqualTo("/quarters/q1")).willReturn(ok())
        )

        dataSource.removeQuarter("q1")

        wireMockServer.verify(deleteRequestedFor(urlEqualTo("/quarters/q1")))
    }

    @Test
    fun addQuarter_sendsMappedRequestAndReturnsMappedResponse() = runBlocking {
        wireMockServer.stubFor(
            post(urlEqualTo("/quarters")).willReturn(
                okJson(
                    """
                    [
                      {
                        "id": "q1",
                        "name": "Abril - Julio 2023",
                        "start_date": 1680318000000,
                        "end_date": 1688184000000,
                        "grade": 5.0,
                        "grade_sum": 5.0,
                        "credits": 6,
                        "credits_sum": 19,
                        "is_current": true,
                        "is_read_only": false,
                        "subjects": [
                          {
                            "id": "s1",
                            "qid": "q1",
                            "code": "MA1111",
                            "name": "MATEMATICAS I",
                            "credits": 4,
                            "grade": 4
                          }
                        ]
                      }
                    ]
                    """.trimIndent()
                )
            )
        )

        val quarter = RecordFixtures.remoteQuarter(
            id = "q1",
            name = "Abril - Julio 2023",
            startDate = 1680318000000L,
            endDate = 1688184000000L,
            grade = 5.0,
            gradeSum = 5.0,
            credits = 6,
            creditsSum = 19,
            isCurrent = true,
            isReadOnly = false,
            subjects = listOf(
                RecordFixtures.remoteSubject(
                    id = "s1",
                    quarterId = "q1",
                    code = "MA1111",
                    grade = 4
                )
            )
        )

        val result = dataSource.addQuarter(quarter)

        wireMockServer.verify(
            postRequestedFor(urlEqualTo("/quarters"))
                .withRequestBody(
                    equalToJson(
                        """
                        {
                          "quarter": 2,
                          "year": 2023,
                          "subjects": [
                            {
                              "code": "MA1111",
                              "grade": 4
                            }
                          ]
                        }
                        """.trimIndent()
                    )
                )
        )
        assertEquals(
            listOf(
                RecordFixtures.remoteQuarter(
                    id = "q1",
                    name = "Abril - Julio 2023",
                    startDate = 1680318000000L,
                    endDate = 1688184000000L,
                    grade = 5.0,
                    gradeSum = 5.0,
                    credits = 6,
                    creditsSum = 19,
                    isCurrent = true,
                    isReadOnly = false,
                    subjects = listOf(
                        RecordFixtures.remoteSubject(
                            id = "s1",
                            quarterId = "q1",
                            code = "MA1111",
                            grade = 4
                        )
                    )
                )
            ),
            result
        )
    }

    @Test
    fun addQuarter_whenStartDateMonthIsInvalid_throwsIllegalArgumentException() {
        val quarter = RecordFixtures.remoteQuarter(
            startDate = 1675209600000L,
            subjects = listOf(
                RecordFixtures.remoteSubject(code = "MA1111", grade = 3)
            )
        )

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                dataSource.addQuarter(quarter)
            }
        }
        wireMockServer.verify(0, postRequestedFor(urlEqualTo("/quarters")))
    }
}