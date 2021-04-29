package de.stefanbissell.ktorpersistence

import com.github.jasync.sql.db.QueryResult
import com.github.jasync.sql.db.pool.ConnectionPool
import com.github.jasync.sql.db.postgresql.PostgreSQLConnection
import com.github.javafaker.Faker
import kotlinx.coroutines.future.await
import java.util.UUID
import kotlin.random.Random

class Books(
    private val connectionPool: ConnectionPool<PostgreSQLConnection>
) {

    private val faker = Faker()

    suspend fun searchBooks(query: String) =
        sendPreparedStatement(
            """
            SELECT id, title, author, synopsis, stock 
            FROM test
            WHERE search @@ to_tsquery(?);
        """.trimIndent(),
            listOf(query)
        )
            .rows
            .joinToString(separator = " \n") { it.joinToString(separator = " - ") }

    suspend fun getBooks() =
        sendPreparedStatement("SELECT id, title, author, synopsis, stock FROM test;")
            .rows
            .joinToString(separator = " \n") { it.joinToString(separator = " - ") }

    suspend fun addBook() {
        val id = sendPreparedStatement(
            """
            INSERT INTO test (id, title, author, synopsis, stock) 
            VALUES (?, ?, ?, ?, ?)
            RETURNING id;
        """.trimIndent(),
            faker.book().let {
                listOf<Any>(
                    UUID.randomUUID().toString(),
                    it.title(),
                    it.author(),
                    TestTexts.random(),
                    Random.nextInt(1000)
                )
            }
        ).rows[0][0].toString()
        sendQuery(
            """
            UPDATE test
            SET search = (to_tsvector(title) || to_tsvector(author) || to_tsvector(synopsis))
            WHERE id = '$id';
        """.trimIndent()
        )
    }

    suspend fun createBooksTable() {
        sendQuery(
            """
            CREATE TABLE IF NOT EXISTS test (
                id varchar PRIMARY KEY,
                title varchar,
                author varchar,
                synopsis varchar,
                stock integer,
                search tsvector DEFAULT ''::tsvector
            );
        """.trimIndent()
        )
    }

    private suspend fun sendPreparedStatement(query: String, values: List<Any> = emptyList()): QueryResult =
        connectionPool.sendPreparedStatement(query, values).await()

    private suspend fun sendQuery(query: String): QueryResult =
        connectionPool.sendQuery(query).await()
}
