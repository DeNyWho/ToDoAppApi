package com.example.repository

import com.example.data.table.TodoTable
import com.example.data.table.UserTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DataBaseFactory {

    fun init(){
        Database.connect(hikari())

        transaction {
            SchemaUtils.create(UserTable)
            SchemaUtils.create(TodoTable)
        }
    }


    private fun hikari(): HikariDataSource{
        val config = HikariConfig()
        config.driverClassName = System.getenv("JDBC_DRIVER_1")
        config.jdbcUrl = System.getenv("JDBC_DATABASE_URL_1")
        config.maximumPoolSize = 3
        config.isAutoCommit = true
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()
        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(block: () -> T): T=
        withContext(Dispatchers.IO){
            transaction {
                block()
            }
        }

}