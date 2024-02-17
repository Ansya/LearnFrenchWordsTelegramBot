import java.io.File

fun main() {
    val wordsFile = File("words.txt")
    val strings = wordsFile.readLines()
    strings.forEach { println(it) }
}