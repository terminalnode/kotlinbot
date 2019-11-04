package xyz.kazuthecat.coffeebot.settings

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import xyz.kazuthecat.coffeebot.ExposedDBHandler
import xyz.kazuthecat.coffeebot.settings.SettingsDB.settingsdb.id
import xyz.kazuthecat.coffeebot.settings.SettingsDB.settingsdb.name
import xyz.kazuthecat.coffeebot.settings.SettingsDB.settingsdb.value

class SettingsDB(dbHandler: ExposedDBHandler) : SettingsAbstract() {
  object settingsdb : Table() {
    val id = varchar("id", 30).primaryKey()
    val name = varchar("name", 30).primaryKey()
    val value = text("value")
  }

  init {
    println("Size of customSettings: ${customSettings.size}")
    dbHandler.getDBConnection()
    transaction {
      SchemaUtils.create(settingsdb)
      settingsdb.selectAll().forEach {
        if (customSettings[it[name]] == null) { customSettings[it[name]] = HashMap() }
        customSettings[it[name]]!![it[id]] = it[value]
      }
    }
  }

  override fun writeJSON(settingName: String, id: String, value: String?) {
    transaction {
      // Extension to Exposed-framework with UPSERT support:
      // https://medium.com/@OhadShai/first-steps-with-kotlin-exposed-cb361a9bf5ac
      // TODO: Add option to do this without a batch operation
      // TODO: Explore options to move these extensions into ExposedDBHandler

      if (value != null) {
        val newSetting = SettingsUpdate(settingName, id, value)
        settingsdb.batchInsertOnDuplicateKeyUpdate(listOf(newSetting), listOf(settingsdb.value)) {
          batch, setting ->
            batch[settingsdb.name] = setting.name
            batch[settingsdb.id] = setting.owner
            batch[settingsdb.value] = setting.value
        }
      } else {
        settingsdb.deleteWhere { (settingsdb.id eq id) and (name eq settingName) }
      }
    }
  }

  data class SettingsUpdate(
    val name : String,
    val owner : String,
    val value : String
  )

  fun <T : Table, E> T.batchInsertOnDuplicateKeyUpdate(data: List<E>, onDupUpdateColumns: List<Column<*>>, body: T.(BatchInsertUpdateOnDuplicate, E) -> Unit) {
    data.
      takeIf { it.isNotEmpty() }?.
      let {
        val insert = BatchInsertUpdateOnDuplicate(this, onDupUpdateColumns)
        data.forEach {
          insert.addBatch()
          body(insert, it)
        }
        TransactionManager.current().exec(insert)
      }
  }
}

class BatchInsertUpdateOnDuplicate(table: Table, val onDupUpdate: List<Column<*>>) : BatchInsertStatement(table, false) {
  override fun prepareSQL(transaction: Transaction): String {
    val onUpdateSQL = if (onDupUpdate.isNotEmpty()) {
      " ON DUPLICATE KEY UPDATE " + onDupUpdate.joinToString { "${transaction.identity(it)}=VALUES(${transaction.identity(it)})" }
    } else ""
    return super.prepareSQL(transaction) + onUpdateSQL
  }
}
