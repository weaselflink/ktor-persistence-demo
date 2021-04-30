package de.stefanbissell.ktorpersistence

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.serialization.json
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

    install(ContentNegotiation) {
        json()
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
        get("/search/{query}") {
            call.respondText(database.books.search(call.parameters["query"]!!))
        }
        get("/get/{id}") {
            database.books.get(call.parameters["id"]!!)
                ?.also { call.respondText(it) }
                ?: call.respondNotFound()
        }
    }
}

private suspend fun ApplicationCall.respondNotFound() {
    response.status(HttpStatusCode.NotFound)
    respondText("not found")
}

private fun initDb(block: suspend Books.() -> Unit) =
    Database().apply {
        runBlocking {
            books.block()
        }
    }

