package de.stefanbissell.ktorpersistence

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.NotFoundException
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.netty.EngineMain
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

fun configuredJson() =
    Json {
        prettyPrint = true
    }

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.module() {

    install(StatusPages) {
        exception<NotFoundException> { cause ->
            cause.printStackTrace()
            call.respond(HttpStatusCode.NotFound, cause.message ?: "")
        }
        exception<Throwable> { cause ->
            cause.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, cause.message ?: "")
        }
    }

    val database = initDb {
        createBooksTable()
        repeat(1000) {
            addBook()
        }
    }

    routing {
        get("/") {
            call.respondText(database.books.getAll())
        }
        get("/error") {
            throw Exception("for test")
        }
        get("/search/{query}") {
            call.respondText(database.books.search(call.parameters["query"]!!))
        }
        get("/get/{id}") {
            database.books.get(call.parameters["id"]!!)
                ?.also { call.respondText(it) }
                ?: throw NotFoundException()
        }
    }
}

private fun initDb(block: suspend Books.() -> Unit) =
    Database().apply {
        runBlocking {
            books.block()
        }
    }

