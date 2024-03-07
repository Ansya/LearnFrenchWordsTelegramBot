import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

const val FREQUENCY_OF_UPDATES: Long = 2000
const val TELEGRAM_API_URL = "https://api.telegram.org/bot"

const val CALLBACK_DATA_STATISTICS_CLICKED = "statistics_clicked"
const val CALLBACK_DATA_LEARN_WORDS_CLICKED = "learn_words_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"

class TelegramBotService(
    val botToken: String,
) {
    private val client = HttpClient.newBuilder().build()
    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$TELEGRAM_API_URL$botToken/getUpdates?offset=$updateId"
        val request = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendMessage(chatId: String, message: String): String {
        val encoded = URLEncoder.encode(
            message,
            StandardCharsets.UTF_8
        )
        println(encoded)
        val urlSendMessage = "$TELEGRAM_API_URL$botToken/sendMessage?chat_id=$chatId&text=$encoded"
        val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendMenu(chatId: String): String {
        val urlSendMenu = "$TELEGRAM_API_URL$botToken/sendMessage?chat_id=$chatId"
        val sendMenuBody = """
            {
              "chat_id":$chatId,
              "text":"Основное меню",
              "reply_markup":{
                "inline_keyboard":[
                  [
                    {
                      "text":"Учить слова",
                      "callback_data":"$CALLBACK_DATA_LEARN_WORDS_CLICKED"
                    },
                    {
                      "text":"Статистика",
                      "callback_data":"$CALLBACK_DATA_STATISTICS_CLICKED"
                    }
                  ]
                ]
              }
            }
        """.trimIndent()

        val request = HttpRequest.newBuilder().uri(URI.create(urlSendMenu))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun checkNextQuestionAndSend(chatId: String, trainer: LearnWordsTrainer): String {
        val question = trainer.getNextQuestion()
        if (question == null) {
            sendMessage(chatId, "Вы уже выучили все слова.")
            return ""
        }

        val urlSendQuestion = "$TELEGRAM_API_URL$botToken/sendMessage?chat_id=$chatId"
        var sendQuestionBody = """
            {
              "chat_id":$chatId,
              "text":"Учим слово\n${question.correctAnswer.original}",
              "reply_markup":{
                "inline_keyboard":[
        """.trimIndent()
        question.variants.mapIndexed { index: Int, word: Word ->
            if (index != 0) sendQuestionBody += ","
            sendQuestionBody += """
                [{
                    "text":"${word.translate}",
                    "callback_data":"${CALLBACK_DATA_ANSWER_PREFIX}$index"
                }]
            """.trimIndent()
        }
        sendQuestionBody += "]}}"

        val request = HttpRequest.newBuilder().uri(URI.create(urlSendQuestion))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendQuestionBody))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }
}

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
        val text = messageTextRegex.find(updates)?.groups?.get(1)?.value
        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value
        val data = dataRegex.find(updates)?.groups?.get(1)?.value ?: ""

        if (text != null && chatId != null) {
            if (text.lowercase() == "hello" || text.lowercase() == "hi") {
                tgBotService.sendMessage(chatId, "Hello")
            }
            if (text.lowercase() == "menu" || text == "/start") {
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
}
