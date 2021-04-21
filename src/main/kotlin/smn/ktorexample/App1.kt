package smn.ktorexample

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.ktorApplication1() {
    routing {
        get("/helloworld") {
            call.respondText("Hello, world!")
        }
    }
}