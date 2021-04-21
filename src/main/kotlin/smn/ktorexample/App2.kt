package smn.ktorexample

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.ktorApplication2() {
    exampleKtorKeyValueApplication(
        Application2ProductionRegistry
    )
}

fun Application.exampleKtorKeyValueApplication(
    serviceRegistry: KeyValueApplicationServiceRegistry,
    installMoreThings: Application.() -> Unit = {}
) {
    routing {
        route("/keyvalue/") {
            get("{keyParam}") {
                val key = call.parameters["keyParam"]!!
                val result = serviceRegistry.keyValueStorageService.get(key)

                if (result == null) {
                    call.respond(HttpStatusCode.NotFound, "No such key")
                } else {
                    call.respondText(result)
                }
            }

            put("{keyParam}") {
                val key = call.parameters["keyParam"]!!
                val value = call.receive<String>()

                serviceRegistry.keyValueStorageService.put(key, value)

                call.respond(HttpStatusCode.OK)
            }
        }
    }

    installMoreThings()
}

interface KeyValueApplicationServiceRegistry {
    val keyValueStorageService: KeyValueStorageService
}

object Application2ProductionRegistry: KeyValueApplicationServiceRegistry {
    override val keyValueStorageService: KeyValueStorageService by lazy { KeyValueStorageServiceMapImpl() }
}

interface KeyValueStorageService {
    fun put(key: String, value: String)
    fun get(key: String): String?
}

class KeyValueStorageServiceMapImpl: KeyValueStorageService {
    private val map = mutableMapOf<String, String>()
    override fun put(key: String, value: String) {
        map[key] = value
    }

    override fun get(key: String): String? {
        return map[key]
    }
}