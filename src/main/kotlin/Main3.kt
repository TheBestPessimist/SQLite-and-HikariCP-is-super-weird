package org.example3

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.nio.file.Path
import java.sql.Statement
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.time.Duration.Companion.seconds


val a = arrayOf(
    "PRAGMA foreign_keys=true",
    "PRAGMA auto_vacuum=INCREMENTAL",
    "PRAGMA incremental_vacuum=10",
    "PRAGMA journal_mode=WAL",
    "PRAGMA reverse_unordered_selects=true",
    "PRAGMA synchronous=NORMAL",
    "PRAGMA temp_store=MEMORY",
    "PRAGMA jdbc.explicit_readonly=false",
    "PRAGMA busy_timeout=5",
    "PRAGMA encoding='UTF-8'",
    "PRAGMA integrity_check",
)



@OptIn(ExperimentalPathApi::class)
fun main() {
    Path.of("db").deleteRecursively()
    Path.of("db").createDirectories()


    val hikariConfig = HikariConfig().apply {
//        dataSource == alternative: i could try to create the sqlite datasource and give it to hikari! maybe that's the best, and then the workaround for auto_vacuum is only in the datasource, nothing to do for hikari
        jdbcUrl = "jdbc:sqlite:db/hikari.db"
        maximumPoolSize = 5
        maxLifetime = 30.seconds.inWholeMilliseconds
//        this.cus
    }

    val ds = HikariDataSource(hikariConfig)
    prepareDB(ds, a)

    val s = ds.getConnection().createStatement()
    queries(s)

}

fun prepareDB(ds: HikariDataSource, p: Array<String>) {
    p.forEach {
        println("aaaaaaaa" + it)
        val c = ds.getConnection()
        runCatching {

            val rs = c.createStatement().executeQuery(it)
            while (rs.next()) {
                val v = rs.getString(1)
                println("$it: $v")
            }
        }
        c.close()
    }
    ds.close()
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
