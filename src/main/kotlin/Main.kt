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

    do {
        println("Меню:")
        println("1 – Учить слова")
        println("2 – Статистика")
        println("0 – Выход")
        println()
        println("Введите номер выбранного пункта меню:")
        val menuitem = readln()

        when(menuitem) {
            "1" -> println("1")
            "2" -> println(getStatistics(dictionary))
            "0" -> println("Выход")
            else -> println("Введен неверный пункт меню, попробуйте еще раз")
        }

    } while (menuitem != "0")
}

fun getStatistics(dictionary : List<Word>) : String {
    val learnedWordsCount = dictionary.filter {it.correctAnswersCount == 3}.count()
    val allWordsCount = dictionary.count()
    return "Выучено $learnedWordsCount из $allWordsCount слов | " +
            "${(learnedWordsCount * 100) / allWordsCount}%"
}
