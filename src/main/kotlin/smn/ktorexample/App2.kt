package smn.ktorexample

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

internal const val requestPathKeyValue = "/keyvalue"

fun Application.exampleKtorKeyValueApplication(
    serviceRegistry: KeyValueApplicationServiceRegistry,
    installMoreThings: Application.() -> Unit = {}
) {
    routing {
        route("$requestPathKeyValue") {
            get("/{keyParam}") {
                val key = call.parameters["keyParam"]!!
                val result = serviceRegistry.keyValueStorageService.get(key)

                if (result == null) {
                    call.respond(HttpStatusCode.NotFound, "No such key")
                } else {
                    call.respondText(result)
                }
            }

            put("/{keyParam}") {
                val key = call.parameters["keyParam"]!!
                val value = call.receive<String>()

                serviceRegistry.keyValueStorageService.put(key, value)

                call.respond(HttpStatusCode.OK)
            }
        }
    }

    installMoreThings()
}

//The actual application for "production"
fun Application.ktorApplication2() {
    exampleKtorKeyValueApplication(
        Application2ProductionRegistry()
    )
}

//Service registry for the KeyValue-application. It needs a storage service.
interface KeyValueApplicationServiceRegistry {
    val keyValueStorageService: KeyValueStorageService
}

//Production ready registry for the KeyValue-application. Specifies implementation.
class Application2ProductionRegistry: KeyValueApplicationServiceRegistry {
    override val keyValueStorageService: KeyValueStorageService by lazy {
        KeyValueStorageServiceMapImpl()
    }
}

//The service required by the KeyValue-application
interface KeyValueStorageService {
    fun put(key: String, value: String)
    fun get(key: String): String?
}

//The "production ready" implementation of the KeyValue service.
class KeyValueStorageServiceMapImpl: KeyValueStorageService {
    private val map = mutableMapOf<String, String>()
    override fun put(key: String, value: String) {
        map[key] = value
    }

    override fun get(key: String): String? {
        return map[key]
    }
}
