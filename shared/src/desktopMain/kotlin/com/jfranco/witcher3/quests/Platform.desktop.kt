package com.jfranco.witcher3.quests

import java.io.InputStream

class JvmPlatform : Platform {
    override val name: String = "JVM version: ${System.getProperty("java.version")}"
}

internal actual fun getPlatform(): Platform = JvmPlatform()

internal actual fun getFileInputStream(): InputStream {
    TODO("Not yet implemented")
}