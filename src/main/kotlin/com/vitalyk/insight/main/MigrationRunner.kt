package com.vitalyk.insight.main

import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.skife.jdbi.v2.DBI

// BAC (Bank of America) appears to have the highest volume stock
// and had over 1 billion shares traded for 3 days in 2009,
// which is still roughly 2 times less than MAX_INT.

// For intraday volume `int` is plenty, while for daily volume `bigint` is prudent,
// and for weekly/monthly it's absolutely necessary.

class MigrationRunner(dbi: DBI) {
    init {
        dbi.useHandle {
            val jdbcConnection = JdbcConnection(it.connection)
            val database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection)
            val liquibase = Liquibase("migrations.json", ClassLoaderResourceAccessor(), database)
            liquibase.update("staging")
        }
    }
}