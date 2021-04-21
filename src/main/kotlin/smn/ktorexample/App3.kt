package smn.ktorexample

import com.mchange.v2.c3p0.ComboPooledDataSource
import io.ktor.application.*
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.BaseTable
import org.ktorm.schema.varchar
import org.ktorm.support.postgresql.PostgreSqlDialect
import org.ktorm.support.postgresql.insertOrUpdate

fun Application.ktorApplication3() {
    exampleKtorKeyValueApplication(
        Application3ProductionRegistry
    )
}

object Application3ProductionRegistry: KeyValueApplicationServiceRegistry {
    override val keyValueStorageService: KeyValueStorageService by lazy {
        KeyValueStorageServicePostgresImpl(EmbeddedPostgres().db)
    }
}

class KeyValueStorageServicePostgresImpl(val db: Database): KeyValueStorageService {
    override fun put(key: String, value: String) {
        db.useTransaction {
            db.insertOrUpdate(KeyValueTable) {
                set(it.key, key)
                set(it.value, value)
                onConflict {
                    set(it.value, value)
                }
            }
        }
    }

    override fun get(key: String): String? {
        return db.useTransaction {
            db.sequenceOf(KeyValueTable).find { it.key eq key }?.value
        }
    }
}

////////////////Domain model

data class KeyValueMapping(val key: String, val value: String)

////////////////Database implementation and DB-mapping

open class EmbeddedPostgres {
    private val predefinedDbAndUserName = "postgres"
    private val predefinedPassword = ""
    private val dbEndpoint by lazy { "localhost:${embeddedPostgres.port}" }
    private val embeddedPostgres by lazy {
        val db = EmbeddedPostgres.start()
        db.getPostgresDatabase()
        db
    }

    protected val datasource by lazy {
        val datasource = ComboPooledDataSource()
        datasource.driverClass = "org.postgresql.Driver"
        datasource.jdbcUrl = "jdbc:postgresql://$dbEndpoint/$predefinedDbAndUserName"
        datasource.user = predefinedDbAndUserName
        datasource.password = predefinedPassword
        datasource
    }

    val db by lazy {
        val database = connectToDatabase()

        val schema = "CREATE TABLE IF NOT EXISTS keyval (key VARCHAR(40) PRIMARY KEY, value VARCHAR(40) NOT NULL)"
        database.useConnection { conn ->
            conn.prepareStatement(schema).execute()
        }

        database
    }

    internal open fun connectToDatabase(): Database {
        return Database.connect(
            datasource,
            dialect = PostgreSqlDialect()
        )
    }
}

object KeyValueTable: BaseTable<KeyValueMapping>("keyval") {
    val key = varchar("key").primaryKey()
    val value = varchar("value")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean) = KeyValueMapping (
        key = row[key]!!,
        value = row[value]!!
    )
}