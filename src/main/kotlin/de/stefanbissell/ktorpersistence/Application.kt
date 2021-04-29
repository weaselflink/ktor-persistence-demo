package de.stefanbissell.ktorpersistence

import com.github.jasync.sql.db.postgresql.PostgreSQLConnectionBuilder
import com.github.javafaker.Faker
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.netty.EngineMain
import kotlinx.coroutines.runBlocking
import org.testcontainers.containers.PostgreSQLContainer
import java.util.concurrent.TimeUnit

val sqlContainer = CustomSQLContainer()

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.module() {

    val books = initDb {
        createBooksTable()
        repeat(1000) {
            addBook()
        }
    }

    routing {
        get("/") {
            call.respondText(books.getBooks())
        }
        get("/search/{query}") {
            call.respondText(books.searchBooks(call.parameters["query"]!!))
        }
    }
}

private fun initDb(block: suspend Books.() -> Unit): Books {
    sqlContainer.start()
    println(sqlContainer.jdbcUrl)
    return PostgreSQLConnectionBuilder.createConnectionPool {
        username = sqlContainer.username
        host = sqlContainer.host
        port = sqlContainer.firstMappedPort
        password = sqlContainer.password
        database = sqlContainer.databaseName
        maxActiveConnections = 100
        maxIdleTime = TimeUnit.MINUTES.toMillis(15)
        maxPendingQueries = 10_000
        connectionValidationInterval = TimeUnit.SECONDS.toMillis(30)
    }.let { pool ->
        Books(pool).apply {
            runBlocking {
                block()
            }
        }
    }
}

class CustomSQLContainer : PostgreSQLContainer<CustomSQLContainer>("postgres:13.2-alpine") {
    init {
        withDatabaseName("my_db")
        withUsername("test")
        withPassword("test")
        withCommand("postgres")
    }
}

