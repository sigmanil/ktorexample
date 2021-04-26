package smn.ktorexample

import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class App2Test {

    @Test
    fun testCarrotCake() {
        val testCake = "carrotcake"

        withExampleKtorApplication2 {
            handleGet("$requestPathKeyValue/cake") {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }

            handlePut("$requestPathKeyValue/cake", testCake) {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleGet("$requestPathKeyValue/cake") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(testCake, response.content)
            }
        }
    }

    @Test
    fun testBrownie() {
        val testCake = "brownie"

        withExampleKtorApplication2 {
            handleGet("$requestPathKeyValue/cake") {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }

            handlePut("$requestPathKeyValue/cake", testCake) {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleGet("$requestPathKeyValue/cake") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(testCake, response.content)
            }
        }
    }
}

fun withExampleKtorApplication2(
    test: TestApplicationEngine.() -> Unit
) {
    withTestApplication({
        ktorApplication2()
    }) {
        test()
    }
}

fun TestApplicationEngine.handlePut(
    requestUri: String,
    requestBody: String,
    handleApplicationCall: TestApplicationCall.() -> Unit
) {
    with(handleRequest {
        this.uri = requestUri
        this.method = HttpMethod.Put
        addHeader(HttpHeaders.ContentType, ContentType.Text.Plain.toString())
        setBody(requestBody)
    }) {
        handleApplicationCall()
    }
}
