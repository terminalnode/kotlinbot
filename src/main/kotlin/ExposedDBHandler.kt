package xyz.kazuthecat.coffeebot

import org.jetbrains.exposed.sql.Database

class ExposedDBHandler constructor(
  dbAddr : String,
  private val dbUser : String,
  private val dbPass : String){
  private val dbURL = "jdbc:mysql://${dbAddr}/coffeedb"
  private val dbDriver = "com.mysql.jdbc.Driver"

  fun getDBConnection() : Database {
    return Database.connect(dbURL, driver = dbDriver, user = dbUser, password = dbPass)
  }

  // This seems kind of useless given how simple Exposed is to use, keeping it in case we need it later.
  // fun createSchema(table: Table) {
  //   getDBConnection()
  //   transaction {
  //     SchemaUtils.create(table)
  //   }
  // }
}