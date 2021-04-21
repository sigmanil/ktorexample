package smn.ktorexample

import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class App1Test {

    @Test
    fun testHelloWorld() {
        withTestApplication({
            ktorApplication1()
        }) {
            with(handleRequest {
                this.uri = "/helloworld"
                this.method = HttpMethod.Get
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Hello, world!", response.content)
            }
        }
    }
}