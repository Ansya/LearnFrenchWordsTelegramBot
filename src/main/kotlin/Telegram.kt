import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val FREQUENCY_OF_UPDATES: Long = 2000

class TelegramBotService(
    val botToken: String,
) {
    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendMessage(chatId: String, text: String): String {
        val urlSendMessage = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$text"
        val client = HttpClient.newBuilder().build()
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
