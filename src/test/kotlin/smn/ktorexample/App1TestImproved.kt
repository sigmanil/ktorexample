package smn.ktorexample

import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class App1TestImproved {
    @Test
    fun testHelloWorld() {
        withExampleKtorApplication1 {
            handleGet("/helloworld") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Hello, world!", response.content)
            }
        }
    }
}


//Helper functions to be reused across all tests - would be in separate file in a real code base.

fun withExampleKtorApplication1(
    test: TestApplicationEngine.() -> Unit
) {
    withTestApplication({
        ktorApplication1()
    }) {
        test()
    }
}

fun TestApplicationEngine.handleGet(
    requestUri: String,
    handleApplicationCall: TestApplicationCall.() -> Unit
) {
    with(handleRequest {
        this.uri = requestUri
        this.method = HttpMethod.Get
    }) {
        handleApplicationCall()
    }
}