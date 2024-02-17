import java.io.File

data class Word(
    val original :String,
    val translate :String,
    var correctAnswersCount :Int = 0,
)
fun main() {
    val wordsFile = File("words.txt")
    val strings = wordsFile.readLines()
    val dictionary = mutableListOf<Word>()
    strings.forEach {
        val line = it.split("|")
        val word = Word(
            original = line[0],
            translate = line[1],
            correctAnswersCount = line[2].toIntOrNull() ?: 0
        )
        dictionary.add(word)
    }

    println(dictionary)

}
