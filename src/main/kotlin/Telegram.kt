import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val FREQUENCY_OF_UPDATES: Long = 2000
const val TELEGRAM_API_URL = "https://api.telegram.org/bot"

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

    fun sendMessage(chatId: String, text: String): String {
        val urlSendMessage = "$TELEGRAM_API_URL$botToken/sendMessage?chat_id=$chatId&text=$text"
        val request = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()

    }
}

fun main(args: Array<String>) {
    val botToken = args[0]
    var updateId = 0
    val tgBotService = TelegramBotService(botToken)

    while (true) {
        Thread.sleep(FREQUENCY_OF_UPDATES)
        val updates = tgBotService.getUpdates(updateId)
        println(updates)

        val updateIdRegex: Regex = "\"update_id\":(.+?),".toRegex()
        var groups = updateIdRegex.find(updates)?.groups
        updateId = (groups?.get(1)?.value?.toInt() ?: 0) + 1
        println(updateId)

        val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
        groups = messageTextRegex.find(updates)?.groups
        val text = groups?.get(1)?.value
        println(text)

        if (text != null) {
            if (text.lowercase() == "hello" || text.lowercase() == "hi") {
                val chatIdRegex: Regex = "\"chat\":\\{\"id\":(.+?),".toRegex()
                groups = chatIdRegex.find(updates)?.groups
                val chatId = groups?.get(1)?.value
                if (chatId != null) {
                    tgBotService.sendMessage(chatId, "Hello")
                }
            }
        }
    }
}
