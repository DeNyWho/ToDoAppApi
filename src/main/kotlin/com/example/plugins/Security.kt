package com.example.plugins

import com.example.auth.JwtService
import com.example.auth.MySession
import com.example.auth.hashPassword
import com.example.repository.TodoRepository
import com.example.repository.UserRepository
import com.example.routes.todoRoute
import com.example.routes.userRoute
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.routing.*
import io.ktor.sessions.*
import kotlin.collections.set


fun Application.configureSecurity() {

    val userDb = UserRepository()
    val jwt = JwtService()
    val todoDb = TodoRepository()
    val hashFunction = {s:String -> hashPassword(s)}

    install(Sessions) {
        cookie<MySession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }
    install(Authentication) {
        jwt("jwt") {
            verifier(jwt.verifier)
            realm = "Todo Server"
            validate {
                val payload = it.payload
                val claim = payload.getClaim("id")
                val claimString = claim.asInt()
                val user = userDb.findUserById(claimString)
                user
            }
        }
    }

    routing {
        userRoute(userDb, todoDb, jwt, hashFunction)
        todoRoute(userDb, todoDb)
    }


//
//    authentication {
//        jwt {
//            val jwtAudience = environment.config.property("jwt.audience").getString()
//            realm = environment.config.property("jwt.realm").getString()
//            verifier(
//                JWT
//                    .require(Algorithm.HMAC256("secret"))
//                    .withAudience(jwtAudience)
//                    .withIssuer(environment.config.property("jwt.domain").getString())
//                    .build()
//            )
//            validate { credential ->
//                if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
//            }
//        }
//    }
//
//    routing {
//        get("/session/increment") {
//            val session = call.sessions.get<MySession>() ?: MySession()
//            call.sessions.set(session.copy(count = session.count + 1))
//            call.respondText("Counter is ${session.count}. Refresh to increment.")
//        }
//    }
}
