package com.jfranco.newmultiplatformapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform