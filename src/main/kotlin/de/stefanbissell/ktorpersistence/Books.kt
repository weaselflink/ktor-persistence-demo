package de.stefanbissell.ktorpersistence

import com.github.jasync.sql.db.QueryResult
import com.github.jasync.sql.db.RowData
import com.github.jasync.sql.db.pool.ConnectionPool
import com.github.jasync.sql.db.postgresql.PostgreSQLConnection
import com.github.javafaker.Faker
import kotlinx.coroutines.future.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import java.util.UUID
import kotlin.random.Random

class Books(
    private val connectionPool: ConnectionPool<PostgreSQLConnection>
) {

    private val faker = Faker()

    suspend fun getAll() =
        sendPreparedStatement("SELECT id, title, author, synopsis, stock FROM test;")
            .rows
            .asBooks()
            .toJson()

    suspend fun get(id: String) =
        sendPreparedStatement(
            """
                SELECT id, title, author, synopsis, stock 
                FROM test
                WHERE id = ?;
            """.trimIndent(),
            listOf(id)
        )
            .rows
            .firstOrNull()
            ?.asBook()
            ?.toJson()

    suspend fun search(query: String) =
        sendPreparedStatement(
            """
                SELECT id, title, author, synopsis, stock 
                FROM test
                WHERE search @@ to_tsquery(?);
            """.trimIndent(),
            listOf(query)
        )
            .rows
            .asBooks()
            .toJson()

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
        sendPreparedStatement(
            """
                UPDATE test
                SET search = (to_tsvector(title) || to_tsvector(author) || to_tsvector(synopsis))
                WHERE id = ?;
            """.trimIndent(),
            listOf(id)
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

    private fun List<RowData>.asBooks() =
        map {
            it.asBook()
        }

    private fun RowData.asBook() =
        Book(
            id = get("id") as String,
            title = get("title") as String,
            author = get("author") as String,
            synopsis = get("synopsis") as String,
            stock = get("stock") as Int
        )
}

@Serializable
data class Book(
    val id: String,
    val title: String,
    val author: String,
    val synopsis: String,
    val stock: Int
) {
    fun toJson(): String = configuredJson().encodeToString(serializer(), this)
}

fun List<Book>.toJson() = configuredJson().encodeToString(ListSerializer(Book.serializer()), this)
