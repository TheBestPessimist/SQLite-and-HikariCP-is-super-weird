package org.example4

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.sqlite.SQLiteConfig
import java.nio.file.Path
import java.sql.ResultSet
import java.sql.Statement
import javax.sql.DataSource
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds


@OptIn(ExperimentalPathApi::class)
fun cleanDbFolder(): Unit {
    Path.of("db").deleteRecursively()
    Path.of("db").createDirectories()
}

/**
 * I'm going to go ahead and say FUCK SQLite and its stupid idiosyncrasies. Look at the fucking crap i need to do to init a database with some SANE settings.
 */
fun prepareDb(): DataSource {
    val sqliteConfig = SQLiteConfig().apply {
        enforceForeignKeys(true)
//        incrementalVacuum(10) // technical: Dont use this, it breaks vacuum. see below.
//        setJournalMode(SQLiteConfig.JournalMode.WAL) // technical: Dont use this, it breaks vacuum. see below.
        enableReverseUnorderedSelects(true)
        setSynchronous(SQLiteConfig.SynchronousMode.NORMAL)
        setTempStore(SQLiteConfig.TempStore.MEMORY)
        busyTimeout = 5.milliseconds.inWholeMilliseconds.toInt()
        setEncoding(SQLiteConfig.Encoding.UTF8)
    }
    println("SQLiteConfig: ${sqliteConfig.toProperties()}")

    val hikariConfig = HikariConfig().apply {
        jdbcUrl = "jdbc:sqlite:db/hikari.db"
        maximumPoolSize = 5
        maxLifetime = 30.seconds.inWholeMilliseconds
        dataSourceProperties = sqliteConfig.toProperties()
    }

    val ds = HikariDataSource(hikariConfig)

    val onDatabaseStartupPragmas = arrayOf(
        // needed because the SQLite JDBC driver is abandonware and doesn't support all sqlite pragmas
        "PRAGMA auto_vacuum = INCREMENTAL",
        // technical: journal mode must be set AFTER auto_vacuum, otherwise we cannot set auto_vacuum anymore. SQLite is super weird. WTF
        "PRAGMA journal_mode=WAL",
        "PRAGMA integrity_check",
        "PRAGMA foreign_key_check",
        // technical: incremental_vacuum must be done after WAL and after setting auto_vacuum, else we cannot set auto_vacuum anymore. SQLite is super weird. WTF.
        "PRAGMA incremental_vacuum=10",
    )
    for (pragmaSql in onDatabaseStartupPragmas) {
        val s = """Executing Pragma: >$pragmaSql<"""
        println(s)
        ds.connection.use { c ->
            val rs: ResultSet? = runCatching { c.createStatement().executeQuery(pragmaSql) }.getOrNull()
            if (rs != null) {
                while (rs.next()) {
                    val v = rs.getString(1)
                    println("$s result: >$v<")
                }
            } else {
                println("$s NO result")
            }
        }
    }
    return ds
}

fun main() {
    cleanDbFolder()
    val ds = prepareDb()
//
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
