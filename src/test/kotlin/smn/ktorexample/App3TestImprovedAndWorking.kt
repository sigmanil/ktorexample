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

internal class App3TestImprovedAndWorking {

    @Test
    fun testTesla() {
        val testCar = "Tesla"

        withExampleKtorApplication3ActuallyImprovedByRollback {
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

        withExampleKtorApplication3ActuallyImprovedByRollback {
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
