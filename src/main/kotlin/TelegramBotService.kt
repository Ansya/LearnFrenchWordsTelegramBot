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
const val CALLBACK_DATA_MENU = "menu"

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
        val urlSendMenu = "$TELEGRAM_API_URL$botToken/sendMessage"
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

        val urlSendQuestion = "$TELEGRAM_API_URL$botToken/sendMessage"
        val keyboard = question.variants.mapIndexed { index: Int, word: Word ->
            """
                [{
                    "text":"${word.translate}",
                    "callback_data":"${CALLBACK_DATA_ANSWER_PREFIX}$index"
                }]
            """.trimIndent()
        }.joinToString(",")
        val sendQuestionBody = """
            {
                "chat_id":$chatId,
                "text":"Учим слово\n${question.correctAnswer.original}",
                "reply_markup":{
                    "inline_keyboard":[ 
                        $keyboard,
                        [{
                            "text":"Главное меню",
                            "callback_data":"menu"
                        }]
                     ]
                }
            }
        """.trimIndent()

        val request = HttpRequest.newBuilder().uri(URI.create(urlSendQuestion))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendQuestionBody))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }
}
