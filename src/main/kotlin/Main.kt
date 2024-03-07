
fun Question.asConsoleString(): String {
    val variants = this.variants
        .mapIndexed { index: Int, word: Word ->
            " ${index + 1} - ${word.translate} "
        }
        .joinToString(separator = "\n")
    return "Учим слово \'${this.correctAnswer.original}\'\n" +
            variants + "\n 0 - Вернуться в главное меню.\n -> Выберите перевод: "
}

fun main() {
    val trainer = LearnWordsTrainer()

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
            "1" -> {
                do {
                    val question = trainer.getNextQuestion()
                    if (question == null) {
                        println("Вы уже выучили все слова.")
                        break
                    }

                    println(question.asConsoleString())

                    val userAnswerInput = readln().toIntOrNull()
                    if (userAnswerInput == 0) break

                    if (trainer.checkAnswer(userAnswerInput?.minus(1))) {
                        println("Верно!")
                    } else {
                        println(
                            "Неверный ответ. " +
                                    "${question.correctAnswer.original} - это \'${question.correctAnswer.translate}\'"
                        )
                    }
                } while (true)
            }

            "2" -> {
                val statistics = trainer.getStatistics()
                println(statistics.toString())
            }

            "0" -> println("Выход")

            else -> println("Введен неверный пункт меню, попробуйте еще раз")
        }

    } while (menuitem != "0")
}
