package oqk.ananke.clepsydrae.core

import app.cash.sqldelight.db.SqlDriver
import oqk.ananke.clepsydrae.Database

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): Database {
    val driver = driverFactory.createDriver()
    val database = Database(driver)

    // Do more work with the database (see below).
    return database
}