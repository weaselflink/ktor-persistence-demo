package de.stefanbissell.ktorpersistence

object TestTexts {

    private val rawText = String(ClassLoader.getSystemResourceAsStream("war-and-peace.txt")!!.readBytes())
    private val texts = rawText.split("\n\n").filter { it.length > 20 }

    fun random() =
        texts.random()
}
