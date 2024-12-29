package com.jfranco.witcher3.quests.cli

import com.jfranco.witcher3.quests.Quests
import java.io.FileInputStream


fun main() {
    val foo = FileInputStream("/Users/f.sangiacomo/Downloads/quests.csv").use(::Quests)
    foo.print()
}