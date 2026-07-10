package com.opensplit.usermessage

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

sealed interface UiString {
  @Composable fun content(): String

  data object EmptyString : UiString {
    @Composable override fun content() = ""
  }

  data class StringLiteral(
      val value: String,
  ) : UiString {
    @Composable override fun content() = value
  }

  data class StringRes(
      val res: StringResource,
  ) : UiString {
    @Composable override fun content(): String = stringResource(res)
  }

  data class StringResWithArgument(
      val res: StringResource,
      val formatArgs: Array<out Any>,
  ) : UiString {
    @Composable
    override fun content(): String =
        stringResource(
            res,
            *formatArgs
                .map { arg ->
                  when (arg) {
                    is UiString -> arg.content()
                    else -> arg
                  }
                }
                .toTypedArray(),
        )

    override fun equals(other: Any?): Boolean =
        if (this === other) {
          true
        } else {
          (other is StringResWithArgument) &&
              (res == other.res) &&
              formatArgs.contentEquals(other.formatArgs)
        }

    override fun hashCode(): Int {
      var result = res.hashCode()
      result = 31 * result + formatArgs.contentHashCode()
      return result
    }
  }

  data class Concat(
      val parts: List<UiString>,
  ) : UiString {
    @Composable
    override fun content(): String = buildString { parts.forEach { append(it.content()) } }
  }

  companion object {
    operator fun invoke(value: String): UiString =
        if (value.isEmpty()) {
          EmptyString
        } else {
          StringLiteral(value)
        }

    operator fun invoke(
        res: StringResource,
    ): UiString = StringRes(res)

    operator fun invoke(
        res: StringResource,
        vararg formatArgs: Any,
    ): UiString = StringResWithArgument(res, formatArgs)
  }
}

operator fun UiString.plus(other: UiString): UiString {
  if (this is UiString.EmptyString) return other
  if (other is UiString.EmptyString) return this

  val merged =
      buildList {
            if (this@plus is UiString.Concat) {
              addAll(this@plus.parts)
            } else {
              add(this@plus)
            }

            if (other is UiString.Concat) {
              addAll(other.parts)
            } else {
              add(other)
            }
          }
          .filterNot { it is UiString.EmptyString }

  return when (merged.size) {
    0 -> UiString.EmptyString
    1 -> merged.first()
    else -> UiString.Concat(merged)
  }
}

operator fun UiString.plus(text: String): UiString = this + UiString(text)

operator fun String.plus(ui: UiString): UiString = UiString(this) + ui
