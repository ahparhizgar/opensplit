package com.opensplit.usermessage

import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch

interface MessageShower {
  suspend fun showSnackbarForResult(message: SnackbarMessage): SnackbarResult

  suspend fun showSnackbar(message: SnackbarMessage)
}

class MessageHolder : MessageShower {
  private val messages: Channel<Request> = Channel(capacity = Channel.RENDEZVOUS)

  override suspend fun showSnackbarForResult(message: SnackbarMessage): SnackbarResult {
    val request =
      Request(
        input = message,
        response = CompletableDeferred(),
        job = currentCoroutineContext()[Job],
      )
    messages.send(request)
    return request.response.await()
  }

  override suspend fun showSnackbar(message: SnackbarMessage) {
    messages.send(Request(input = message, response = CompletableDeferred(), job = null))
  }

  suspend fun showOne(block: suspend (SnackbarMessage) -> SnackbarResult) {
    val message = messages.receive()
    if (message.job != null) {
      val scope = CoroutineScope(message.job)
      val worker = scope.launch {
        val result = block(message.input)
        message.response.complete(result)
      }
      worker.join()
    } else {
      val result = block(message.input)
      message.response.complete(result)
    }
  }
}

data class Request(
  val input: SnackbarMessage,
  val response: CompletableDeferred<SnackbarResult>,
  val job: Job?,
)
