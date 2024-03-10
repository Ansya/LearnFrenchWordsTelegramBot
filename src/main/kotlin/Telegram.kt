fun main(args: Array<String>) {
    val botToken = args[0]
    var updateId = 0
    val tgBotService = TelegramBotService(botToken)
    val trainer = LearnWordsTrainer()

    val updateIdRegex: Regex = "\"update_id\":(.+?),".toRegex()
    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val chatIdRegex: Regex = "\"chat\":\\{\"id\":(.+?),".toRegex()
    val dataRegex: Regex = "\"data\":\"(.+?)\"".toRegex()

    while (true) {
        Thread.sleep(FREQUENCY_OF_UPDATES)
        val updates = tgBotService.getUpdates(updateId)
        println(updates)

        updateId = (updateIdRegex.find(updates)?.groups?.get(1)?.value?.toInt() ?: 0) + 1
        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value ?: continue
        val text = messageTextRegex.find(updates)?.groups?.get(1)?.value ?: ""
        val data = dataRegex.find(updates)?.groups?.get(1)?.value ?: ""

        if (text.lowercase() == CALLBACK_DATA_MENU
            || text == "/start"
            || data == CALLBACK_DATA_MENU
        ) {
            tgBotService.sendMenu(chatId)
        }
        if (data == CALLBACK_DATA_STATISTICS_CLICKED) {
            val statistic = trainer.getStatistics()
            tgBotService.sendMessage(chatId, statistic.toString())
        }
        if (data == CALLBACK_DATA_LEARN_WORDS_CLICKED) {
            tgBotService.checkNextQuestionAndSend(chatId, trainer)
        }
        if (data.startsWith(CALLBACK_DATA_ANSWER_PREFIX)) {
            val answerIndex = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
            if (trainer.checkAnswer(answerIndex)) {
                tgBotService.sendMessage(chatId, "Верно!")
            } else {
                tgBotService.sendMessage(
                    chatId,
                    "Неверный ответ. " +
                            "${trainer.question?.correctAnswer?.original} - это " +
                            "\'${trainer.question?.correctAnswer?.translate}\'"
                )
            }
            tgBotService.checkNextQuestionAndSend(chatId, trainer)
        }
    }
}
