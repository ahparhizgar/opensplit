package com.opensplit.util

import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.printToLog
import com.arkivanov.essenty.lifecycle.resume
import com.opensplit.component.TestCContext
import com.opensplit.root.RootComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.Koin

@OptIn(ExperimentalTestApi::class)
context(test: ComposeUiTest)
fun SemanticsNodeInteraction.waitForExistence(
    conditionDescription: String? = null
): SemanticsNodeInteraction {
  try {
    test.waitUntil(
        timeoutMillis = 5000,
        conditionDescription = conditionDescription ?: "wait for existence",
    ) {
      runCatching { fetchSemanticsNode() }.isSuccess
    }
  } catch (e: ComposeTimeoutException) {
    test.onAllNodes(isRoot()).printToLog(tag = "ComposeTest", maxDepth = 10)
    throw e
  }
  return this
}

suspend fun Koin.injectUiRoot(context: TestCContext, resume: Boolean = true): RootComponent =
    withContext(Dispatchers.Main) {
      val root = get<RootComponent.Factory>().create(context)
      if (resume) {
        context.lifecycleRegistry.resume()
      }
      root
    }
