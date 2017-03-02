package main

import com.google.inject.Inject
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.skife.jdbi.v2.DBI

class MigrationRunner(dbi: DBI) {
    init {
        dbi.useHandle {
            val jdbcConnection = JdbcConnection(it.connection)
            val database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection)
            val liquibase = Liquibase("migrations.json", ClassLoaderResourceAccessor(), database)
            liquibase.update("staging")
//
        }
    }
}