import java.io.File

data class Statistics(
    val learnedWordsCount: Int,
    val allWordsCount: Int,
    val percent: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordsTrainer(
    val dictionaryFileName: String = "words.txt",
    val correctAnswersCount: Int = 3,
    val numberOfWordsToChoose: Int = 4,
) {

    private val dictionary = readDictionaryFromFile()
    private var question: Question? = null


    fun getStatistics(): Statistics {
        val learnedWordsCount = dictionary.count { it.correctAnswersCount >= correctAnswersCount }
        val allWordsCount = dictionary.count()
        val percent = (learnedWordsCount * 100) / allWordsCount
        return Statistics(learnedWordsCount, allWordsCount, percent)
    }

    fun getNextQuestion(): Question? {
        val unlearnedWords = dictionary.filter { it.correctAnswersCount < correctAnswersCount }
        if (unlearnedWords.isEmpty()) return null

        var wordsForAnswer = unlearnedWords.shuffled().take(numberOfWordsToChoose)
        val wordToLearn = wordsForAnswer.random()
        if (wordsForAnswer.count() < numberOfWordsToChoose) {
            val additionalWordsForAnswer = dictionary
                .shuffled()
                .filter { it.correctAnswersCount == correctAnswersCount }
                .take(numberOfWordsToChoose - wordsForAnswer.count())
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
        val wordsFile = File(dictionaryFileName)
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
        val wordsFile = File(dictionaryFileName)
        wordsFile.writeText("")
        dictionary.forEach {
            val line = "${it.original}|${it.translate}|${it.correctAnswersCount}\n"
            wordsFile.appendText(line)
        }
    }
}


