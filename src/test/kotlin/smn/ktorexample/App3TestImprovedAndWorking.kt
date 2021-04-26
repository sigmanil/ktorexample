package smn.ktorexample

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.ktorm.database.Database
import org.ktorm.database.Transaction
import org.ktorm.database.TransactionIsolation
import org.ktorm.database.TransactionManager
import java.sql.Connection

internal class App3TestImprovedAndWorking {

    @Test
    fun testTesla() {
        val testCar = "Tesla"

        withExampleKtorApplication3ActuallyImprovedByRollback {
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

        withExampleKtorApplication3ActuallyImprovedByRollback {
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


fun <T> withExampleKtorApplication3ActuallyImprovedByRollback(test: TestApplicationEngine.() -> T): T {
    return Application3TestRegistry.testWithRollback { testTrans ->
        withTestApplication({
            exampleKtorKeyValueApplication(
                Application3TestRegistry
            ) {
                install(IntegrationTestTransactionKtorFeature) {
                    log.warn("Installing TEST transaction module, this is extremely unsafe in production.")
                    database = Application3TestRegistry.testDb
                    testTransaction = testTrans
                }
            }
        }) {
            test()
        }
    }
}

class IntegrationTestTransactionKtorFeature(configuration: TransactionKtorModuleConfiguration) {
    val db: Database = configuration.database!!
    val testTransaction: Transaction = configuration.testTransaction!!

    class TransactionKtorModuleConfiguration {
        var database: Database? = null
        var testTransaction: Transaction? = null
    }

    suspend fun withTransaction(context: PipelineContext<Unit, ApplicationCall>) {
        db.run {
            (db.transactionManager as
                IntegrationTestTransactionManager).setTransaction(testTransaction)

            //Again, in a serious environment, think about isolation
            db.useTransaction {
                context.proceed()
            }
        }
    }

    // Implements ApplicationFeature as a companion object.
    companion object Feature :
        ApplicationFeature<ApplicationCallPipeline, TransactionKtorModuleConfiguration, IntegrationTestTransactionKtorFeature> {
        // Creates a unique key for the feature.
        override val key = AttributeKey<IntegrationTestTransactionKtorFeature>("TransactionKtorModule")

        // Code to execute when installing the feature.
        override fun install(pipeline: ApplicationCallPipeline, configure: TransactionKtorModuleConfiguration.() -> Unit): IntegrationTestTransactionKtorFeature {
            val transactionPhase = PipelinePhase("TransactionPhase")

            // It is responsibility of the install code to call the `configure` method with the mutable configuration.
            val configuration = TransactionKtorModuleConfiguration().apply(configure)

            // Create the feature, providing the mutable configuration so the feature reads it keeping an immutable copy of the properties.
            val feature = IntegrationTestTransactionKtorFeature(configuration)

            pipeline.insertPhaseBefore(ApplicationCallPipeline.Call, transactionPhase)

            // Intercept a pipeline.
            pipeline.intercept(transactionPhase) {
                feature.withTransaction(this)
            }

            return feature
        }
    }
}


//Mostly stolen directly from ktorm JdbcTransactionManager
// - which regrettably isn't open, or a subclass would have sufficed. In a serious
// environment, do some thinking about transaction isolation levels!
class IntegrationTestTransactionManager(val connector: () -> Connection) : TransactionManager {
    private val threadLocal = ThreadLocal<Transaction>()

    //This function is really why we roll our own transaction manager
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