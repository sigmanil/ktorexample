package smn.ktorexample

import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.ktorm.database.Database
import org.ktorm.database.Transaction
import org.ktorm.database.TransactionIsolation
import org.ktorm.database.TransactionManager
import org.ktorm.support.postgresql.PostgreSqlDialect
import java.sql.Connection

internal class App3TestImproved {

    @Test
    fun testTesla() {
        val testCar = "Tesla"

        withExampleKtorApplication3ImprovedByRollback {
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

        withExampleKtorApplication3ImprovedByRollback {
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
            IntegrationTestTransactionManager{
                datasource.connection
            },
            dialect = PostgreSqlDialect()
        )
    }
}

//Mostly stolen directly from ktorm JdbcTransactionManager
// - which regrettably isn't open, or a subclass would have sufficed. In a serious
// environment, do some thinking about transaction isolation levels!
class IntegrationTestTransactionManager(val connector: () -> Connection) : TransactionManager {
    private val threadLocal = ThreadLocal<Transaction>()

    fun setTransaction(trans: Transaction) {
        threadLocal.set(trans)
    }

    override val defaultIsolation = TransactionIsolation.SERIALIZABLE

    override val currentTransaction: Transaction? get() = threadLocal.get()

    override fun newTransaction(isolation: TransactionIsolation?): Transaction {
        if (currentTransaction != null) {
            throw IllegalStateException("Current thread is already in a transaction.")
        }

        return JdbcTransaction(isolation ?: defaultIsolation).apply { threadLocal.set(this) }
    }

    override fun newConnection(): Connection {
        return connector.invoke()
    }

    private inner class JdbcTransaction(private val desiredIsolation: TransactionIsolation) : Transaction {
        private var originIsolation = defaultIsolation.level
        private var originAutoCommit = true

        private val connectionLazy = lazy(LazyThreadSafetyMode.NONE) {
            newConnection().apply {
                try {
                    originIsolation = transactionIsolation
                    if (originIsolation != desiredIsolation.level) {
                        transactionIsolation = desiredIsolation.level
                    }

                    originAutoCommit = autoCommit
                    if (originAutoCommit) {
                        autoCommit = false
                    }
                } catch (e: Throwable) {
                    closeSilently()
                    throw e
                }
            }
        }

        override val connection: Connection by connectionLazy

        override fun commit() {
            if (connectionLazy.isInitialized()) {
                connection.commit()
            }
        }

        override fun rollback() {
            if (connectionLazy.isInitialized() && !connection.isClosed) {
                connection.rollback()
            }
        }

        override fun close() {
            try {
                if (connectionLazy.isInitialized() && !connection.isClosed) {
                    connection.closeSilently()
                }
            } finally {
                threadLocal.remove()
            }
        }

        @Suppress("SwallowedException")
        private fun Connection.closeSilently() {
            try {
                if (originIsolation != desiredIsolation.level) {
                    transactionIsolation = originIsolation
                }
                if (originAutoCommit) {
                    autoCommit = true
                }
            } catch (_: Throwable) {
            } finally {
                try {
                    close()
                } catch (_: Throwable) {
                }
            }
        }
    }
}