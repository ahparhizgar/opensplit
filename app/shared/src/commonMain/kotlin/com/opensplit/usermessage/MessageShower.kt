package com.opensplit.usermessage

import androidx.compose.material3.SnackbarResult
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
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

  suspend fun showAll(block: suspend (SnackbarMessage) -> SnackbarResult) {
    coroutineScope {
      var message: Request? = null
      while (true) {
        message =
            message
                ?: run {
                  log("waiting for message")
                  messages.receive()
                }
        var a: Deferred<Unit>? = null
        val jobScope =
            CoroutineScope(
                currentCoroutineContext() +
                    if (message.job != null) message.job else EmptyCoroutineContext
            )
        val worker =
            jobScope.launch(start = CoroutineStart.UNDISPATCHED) {
              try {
                log("Showing message ${message!!.input.message}")
                val result = block(message!!.input)
                message!!.response.complete(result)
                log("Message completed: ${message!!.input.message}")
                a?.cancel()
              } catch (e: CancellationException) {
                a?.cancelAndJoin()
                throw e
              } finally {
                log("message becomes null")
                message = null
              }
            }

        a =
            async(start = CoroutineStart.UNDISPATCHED) {
              val next = messages.receive()
              if (next.input.message == message!!.input.message) {
                log("cancelling previous message")
                try {
                  message!!.response.cancel(CancellationException("Cancelled by next message"))
                  worker.cancelAndJoin()
                } finally {
                  log("messages becomes next")
                  message = next
                }
              }
            }

        worker.join()
        a.join()
      }
    }
  }
}

data class Request(
    val input: SnackbarMessage,
    val response: CompletableDeferred<SnackbarResult>,
    val job: Job?,
)

fun log(s: String) {
  println("LOG: $s")
}
