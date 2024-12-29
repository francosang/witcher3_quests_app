package com.jfranco.witcher3.quests

import java.io.InputStream

interface Platform {
    val name: String
}

internal expect fun getPlatform(): Platform

internal expect fun getFileInputStream(): InputStream