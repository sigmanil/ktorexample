package smn.ktorexample

import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

fun withExampleKtorApplication3(
    test: TestApplicationEngine.() -> Unit
) {
    withTestApplication({
        ktorApplication3()
    }) {
        test()
    }
}

internal class App3Test {

    @Test
    fun testTesla() {
        val testCar = "Tesla"

        withExampleKtorApplication3 {
            handleGet("/keyvalue/car") {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }

            handlePut("/keyvalue/car", testCar) {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleGet("/keyvalue/car") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(testCar, response.content)
            }
        }
    }

    @Test
    fun testToyota() {
        val testCar = "Toyota"

        withExampleKtorApplication3 {
            handleGet("/keyvalue/car") {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }

            handlePut("/keyvalue/car", testCar) {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleGet("/keyvalue/car") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(testCar, response.content)
            }
        }
    }

}