package me.mucloud.application.mk.serverlauncher.muview.view

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import me.mucloud.application.mk.serverlauncher.muview.gson

fun Application.initRoute() {
    install(CORS){
        anyHost()
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Options)
        allowSameOrigin = true
        allowHeader("Content-Type")
        allowNonSimpleContentTypes = true
    }
    install(ContentNegotiation){ gson }

    initServerRoute()
    initEnvRoute()
}