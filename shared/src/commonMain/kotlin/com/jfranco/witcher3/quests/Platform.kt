package com.jfranco.witcher3.quests

interface Platform {
    val name: String
}

internal expect fun getPlatform(): Platform