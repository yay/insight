package main

import org.skife.jdbi.v2.DBI

fun runDbMigration(db: DBI) {
    val runner = MigrationRunner(db)
}

fun createTableIndex(db: DBI) {
    db.useHandle {
        it.execute("begin")
        it.execute("alter table dailyquotes add primary key (quote_date, market, symbol)")
        it.execute("commit")
    }
}