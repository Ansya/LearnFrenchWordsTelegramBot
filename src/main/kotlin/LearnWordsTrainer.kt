import java.io.File

const val DICTIONARY_FILE_NAME = "words.txt"

data class Statistics(
    val learnedWordsCount: Int,
    val allWordsCount: Int,
    val percent: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer {

    private val dictionary = readDictionaryFromFile()
    private var question: Question? = null


    fun getStatistics(): Statistics {
        val learnedWordsCount = dictionary.count { it.correctAnswersCount == CORRECT_ANSWERS_COUNT_TO_LEARN }
        val allWordsCount = dictionary.count()
        val percent = (learnedWordsCount * 100) / allWordsCount
        return Statistics(learnedWordsCount, allWordsCount, percent)
    }

    fun getNextQuestion(): Question? {
        val unlearnedWords = dictionary.filter { it.correctAnswersCount < CORRECT_ANSWERS_COUNT_TO_LEARN }
        if (unlearnedWords.isEmpty()) return null

        var wordsForAnswer = unlearnedWords.shuffled().take(NUMBER_OF_WORDS_TO_CHOOSE_ANSWER)
        val wordToLearn = wordsForAnswer.random()
        if (wordsForAnswer.count() < NUMBER_OF_WORDS_TO_CHOOSE_ANSWER) {
            val additionalWordsForAnswer = dictionary
                .shuffled()
                .filter { it.correctAnswersCount == CORRECT_ANSWERS_COUNT_TO_LEARN }
                .take(NUMBER_OF_WORDS_TO_CHOOSE_ANSWER - wordsForAnswer.count())
            wordsForAnswer = wordsForAnswer + additionalWordsForAnswer
        }
        wordsForAnswer = wordsForAnswer.shuffled()

        question = Question(wordsForAnswer, wordToLearn)
        return question
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return question?.let {
            val correctAnswerIndex = it.variants.indexOf(it.correctAnswer)
            if (userAnswerIndex == correctAnswerIndex) {
                it.correctAnswer.correctAnswersCount++
                saveDictionaryToFile()
                true
            } else {
                false
            }
        } ?: false
    }

    private fun readDictionaryFromFile(): List<Word> {
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

    private fun saveDictionaryToFile() {
        val wordsFile = File(DICTIONARY_FILE_NAME)
        wordsFile.writeText("")
        dictionary.forEach {
            val line = "${it.original}|${it.translate}|${it.correctAnswersCount}\n"
            wordsFile.appendText(line)
        }
    }
}


