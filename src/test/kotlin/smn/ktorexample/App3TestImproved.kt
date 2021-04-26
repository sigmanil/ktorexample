package smn.ktorexample

import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.ktorm.database.Database
import org.ktorm.database.Transaction
import org.ktorm.support.postgresql.PostgreSqlDialect

internal class App3TestImproved {

    @Test
    fun testTesla() {
        val testCar = "Tesla"

        withExampleKtorApplication3ImprovedByRollback {
            handleGet("$requestPathKeyValue/car") {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }

            handlePut("$requestPathKeyValue/car", testCar) {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleGet("$requestPathKeyValue/car") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(testCar, response.content)
            }
        }
    }

    @Test
    fun testToyota() {
        val testCar = "Toyota"

        withExampleKtorApplication3ImprovedByRollback {
            handleGet("$requestPathKeyValue/car") {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }

            handlePut("$requestPathKeyValue/car", testCar) {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            handleGet("$requestPathKeyValue/car") {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(testCar, response.content)
            }
        }
    }

}


fun <T> withExampleKtorApplication3ImprovedByRollback(test: TestApplicationEngine.() -> T): T {
    return Application3TestRegistry.testWithRollback { testTrans ->
        withTestApplication({
            exampleKtorKeyValueApplication(
                Application3TestRegistry
            )
        }) {
            test()
        }
    }
}

object Application3TestRegistry: KeyValueApplicationServiceRegistry {
    val testDb = EmbeddedTestPostgres().db

    override val keyValueStorageService: KeyValueStorageService by lazy {
        KeyValueStorageServicePostgresImpl(testDb)
    }

    fun <T> testWithRollback(testCode: (transaction: Transaction) -> T): T {
        return testDb.useTransaction { trans ->
            val res = try {
                testCode(trans)
            } finally {
                trans.rollback()
            }
            res
        }
    }
}

class EmbeddedTestPostgres: EmbeddedPostgres() {
    override fun connectToDatabase(): Database {
        return Database(
            IntegrationTestTransactionManager {
                datasource.connection
            },
            dialect = PostgreSqlDialect()
        )
    }
}

