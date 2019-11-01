package xyz.kazuthecat.coffeebot

import org.jetbrains.exposed.sql.*

class ExposedDBHandler constructor(
  private val dbAddr : String,
  private val dbUser : String,
  private val dbPass : String){
  private val dbURL = "jdbc:mysql://${dbAddr}/coffeedb"
  
  public fun getDBConnection() : Database {
    return Database.connect(dbURL, driver = "com.mysql.jdbc.Driver", user = dbUser, password = dbPass)
  }
}