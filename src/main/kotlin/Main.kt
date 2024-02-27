import java.io.File

const val CORRECT_ANSWERS_COUNT_TO_LEARN = 3
const val DICTIONARY_FILE_NAME = "words.txt"
const val NUMBER_OF_WORDS_TO_CHOOSE_ANSWER = 4

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

fun main() {
    val dictionary = readDictionaryFromFile()
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

        when (menuitem) {
            "1" -> startToLearnWords(dictionary)
            "2" -> println(getStatistics(dictionary))
            "0" -> println("Выход")
            else -> println("Введен неверный пункт меню, попробуйте еще раз")
        }

    } while (menuitem != "0")
}

fun readDictionaryFromFile(): List<Word> {
    val wordsFile = File(DICTIONARY_FILE_NAME)
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
    return dictionary
}

fun saveDictionaryToFile(dictionary: List<Word>) {
    val wordsFile = File(DICTIONARY_FILE_NAME)
    wordsFile.writeText("")
    dictionary.forEach {
        val line = "${it.original}|${it.translate}|${it.correctAnswersCount}\n"
        wordsFile.appendText(line)
    }
}

fun getStatistics(dictionary: List<Word>): String {
    val learnedWordsCount = dictionary.count { it.correctAnswersCount == CORRECT_ANSWERS_COUNT_TO_LEARN }
    val allWordsCount = dictionary.count()
    return "Выучено $learnedWordsCount из $allWordsCount слов | " +
            "${(learnedWordsCount * 100) / allWordsCount}%"
}

fun startToLearnWords(dictionary: List<Word>) {
    do {
        val unlearnedWords = dictionary.filter { it.correctAnswersCount < CORRECT_ANSWERS_COUNT_TO_LEARN }
        if (unlearnedWords.isEmpty()) {
            println("Вы уже выучили все слова.")
            break
        }

        var wordsForAnswer = unlearnedWords.shuffled().take(NUMBER_OF_WORDS_TO_CHOOSE_ANSWER)
        val wordToLearn = wordsForAnswer.random()
        if (wordsForAnswer.count() < NUMBER_OF_WORDS_TO_CHOOSE_ANSWER) {
            val additionalWordsForAnswer = dictionary
                .shuffled()
                .filter { it.original !=  wordToLearn.original }
                .take(NUMBER_OF_WORDS_TO_CHOOSE_ANSWER - wordsForAnswer.count())
            wordsForAnswer = wordsForAnswer + additionalWordsForAnswer
        }
        wordsForAnswer.shuffled()
        val indexOfWordToLearn = wordsForAnswer.indexOf(wordToLearn)

        println("Учим слово \'${wordToLearn.original}\'")
        wordsForAnswer.forEachIndexed { i, word ->
            println("${i + 1} - ${word.translate}")
        }
        println("0 - Вернуться в главное меню.")
        print("Выберите перевод: ")

        val answer = readln().toInt()

        if (answer == indexOfWordToLearn + 1) {
            println("Верно!")
            wordToLearn.correctAnswersCount++
            saveDictionaryToFile(dictionary)
        } else {
            println("Неверный ответ.")
        }
    } while (answer != 0)
}
