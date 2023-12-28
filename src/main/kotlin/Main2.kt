package org.example2

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.sqlite.SQLiteConfig
import java.nio.file.Path
import java.sql.Statement
import java.util.*
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


val a = arrayOf(
    "foreign_keys" to "true",
    "auto_vacuum" to "INCREMENTAL",
    "incremental_vacuum" to "10",
    "journal_mode" to "WAL",
    "reverse_unordered_selects" to "true",
    "synchronous" to "NORMAL",
    "temp_store" to "MEMORY",
    "jdbc.explicit_readonly" to "false",
    "busy_timeout" to "5",
    "encoding" to "'UTF-8'",
    "integrity_check" to "",
)

val p = Properties().apply {
    a.forEach {
        this.setProperty(it.first, it.second)
    }
}

@OptIn(ExperimentalPathApi::class)
fun main() {
    Path.of("db").deleteRecursively()
    Path.of("db").createDirectories()

    val config = SQLiteConfig(p)
    println(config.toProperties())


    val hikariConfig = HikariConfig().apply {
        jdbcUrl = "jdbc:sqlite:db/hikari.db"
        maximumPoolSize = 5
        maxLifetime = 30.seconds.inWholeMilliseconds
        dataSourceProperties = config.toProperties()
    }

    val ds = HikariDataSource(hikariConfig)

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
        "PRAGMA integrity_check"
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
