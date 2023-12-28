package org.example

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource
import java.io.File.separator
import java.nio.file.Files.createDirectories
import java.nio.file.Path
import java.sql.Statement
import java.util.Properties
import java.util.concurrent.TimeUnit
import kotlin.io.path.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


val a = arrayOf(
//    "PRAGMA foreign_keys=true",
//    "PRAGMA auto_vacuum=INCREMENTAL",
//    "PRAGMA incremental_vacuum=10",
//    "PRAGMA journal_mode=WAL",
//    "PRAGMA reverse_unordered_selects=true",
//    "PRAGMA synchronous=NORMAL",
    "PRAGMA temp_store=MEMORY",
//    "PRAGMA jdbc.explicit_readonly=false",
//    "PRAGMA busy_timeout=5",
//    "PRAGMA encoding='UTF-8'",
    "PRAGMA integrity_check",

).joinToString(separator = "; \n")


@OptIn(ExperimentalPathApi::class)
fun main() {
    Path.of("db").deleteRecursively()
    Path.of("db").createDirectories()

    val config = SQLiteConfig().apply {
        enableCaseSensitiveLike(true)
        enforceForeignKeys(true)
        incrementalVacuum(10) // ❓ how is this used?
        setJournalMode(SQLiteConfig.JournalMode.WAL)
        enableReverseUnorderedSelects(true)
        setSynchronous(SQLiteConfig.SynchronousMode.NORMAL)
        setTempStore(SQLiteConfig.TempStore.MEMORY)
//        setTransactionMode() // ????
        busyTimeout = 5.milliseconds.inWholeMilliseconds.toInt()
        setEncoding(SQLiteConfig.Encoding.UTF8)
    }
    println(config.toProperties())

//    SQLiteDataSource().

    val hikariConfig = HikariConfig().apply {
//        dataSourceClassName = "org.sqlite.SQLiteDataSource"
        jdbcUrl = "jdbc:sqlite:db/hikari.db"
        maximumPoolSize = 5
        maxLifetime = 30.seconds.inWholeMilliseconds
//        dataSourceProperties = config.toProperties()
        this.connectionInitSql = a
        this.connectionTestQuery = a
    }

    val ds = HikariDataSource(hikariConfig)

    println("aaaaa")



    TimeUnit.SECONDS.sleep(1)

    val s = ds.getConnection().createStatement()
    queries(s)

}

fun queries(s: Statement): Unit {
    val q = arrayOf(
        "PRAGMA reverse_unordered_selects",
        "PRAGMA auto_vacuum",
        "PRAGMA incremental_vacuum",
        "PRAGMA synchronous",
        "PRAGMA busy_timeout",
        "PRAGMA date_string_format",
        "PRAGMA encoding",
        "PRAGMA open_mode",
        "PRAGMA transaction_mode",
        "PRAGMA journal_mode",
        "PRAGMA date_precision",
        "PRAGMA jdbc.explicit_readonly",
        "PRAGMA date_class",
        "PRAGMA foreign_keys",
        "PRAGMA temp_store",
    )

    q.forEach {
        runCatching {

            val rs = s.executeQuery(it)
            while (rs.next()) {
                val v = rs.getString(1)
                println("$it: $v")
            }
        }
    }
}


//fun hikari(): Unit {
//    val config = HikariConfig()
//    config.dataSourceClassName = "org.sqlite.SQLiteDataSource"
//    config.jdbcUrl = "jjdbc:sqlite:hikari.db"
//
//    val ds = HikariDataSource(config)
//}
