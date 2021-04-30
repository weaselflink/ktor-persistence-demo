package de.stefanbissell.ktorpersistence

import com.github.jasync.sql.db.postgresql.PostgreSQLConnectionBuilder
import org.testcontainers.containers.PostgreSQLContainer
import java.util.concurrent.TimeUnit

class Database {

    private val sqlContainer = CustomSQLContainer().also {
        it.start()
    }
    private val connectionPool = PostgreSQLConnectionBuilder.createConnectionPool {
        username = sqlContainer.username
        host = sqlContainer.host
        port = sqlContainer.firstMappedPort
        password = sqlContainer.password
        database = sqlContainer.databaseName
        maxActiveConnections = 100
        maxIdleTime = TimeUnit.MINUTES.toMillis(15)
        maxPendingQueries = 10_000
        connectionValidationInterval = TimeUnit.SECONDS.toMillis(30)
    }

    val books by lazy { Books(connectionPool) }
}

class CustomSQLContainer : PostgreSQLContainer<CustomSQLContainer>("postgres:13.2-alpine") {
    init {
        withDatabaseName("my_db")
        withUsername("test")
        withPassword("test")
        withCommand("postgres")
    }
}
