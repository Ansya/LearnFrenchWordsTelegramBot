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

    do {
        println()
        println("***************************************")
        println("Меню:")
        println("1 – Учить слова")
        println("2 – Статистика")
        println("0 – Выход")
        println()
        println("Введите номер выбранного пункта меню:")
        val menuitem = readln()

        when(menuitem) {
            "1" -> startToLearnWords(dictionary)
            "2" -> println(getStatistics(dictionary))
            "0" -> println("Выход")
            else -> println("Введен неверный пункт меню, попробуйте еще раз")
        }

    } while (menuitem != "0")
}

fun getStatistics(dictionary : List<Word>) : String {
    val learnedWordsCount = dictionary.count { it.correctAnswersCount == 3 }
    val allWordsCount = dictionary.count()
    return "Выучено $learnedWordsCount из $allWordsCount слов | " +
            "${(learnedWordsCount * 100) / allWordsCount}%"
}

fun startToLearnWords(dictionary : List<Word>) {
    val unlearnedWords = dictionary.filter { it.correctAnswersCount < 3 }
    if (unlearnedWords.isEmpty()) {
        println("Вы уже выучили все слова.")
    } else {
        val wordsForAnswer = unlearnedWords.shuffled().take(4)
        val wordToLearn = wordsForAnswer.shuffled().take(1)[0]
        println("Выберите перевод слова \'${wordToLearn.original}\'")
        wordsForAnswer.forEachIndexed { i, word ->
            println("${i + 1} - ${word.translate}")
        }
    }
}
