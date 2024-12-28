package com.jfranco.witcher3.quests

class JvmPlatform : Platform {
    override val name: String = "JVM version: ${System.getProperty("java.version")}"
}

internal actual fun getPlatform(): Platform = JvmPlatform()