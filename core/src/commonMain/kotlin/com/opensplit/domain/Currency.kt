package com.opensplit.domain

import kotlin.math.pow
import kotlin.math.round
import kotlinx.serialization.Serializable

/**
 * An ISO 4217 currency.
 *
 * Amounts in OpenSplit are still stored as [Double] in the currency of the group; this model
 * replaces the hardcoded "IRR" strings that were spread across the UI, so currency identity and
 * amount formatting live in one place. Per-group currency (persisted on the server and in Room) is
 * the natural next step; until then every group uses [default].
 */
@Serializable
enum class Currency(
    /** ISO 4217 alphabetic code, e.g. "IRR". */
    val code: String,
    /** Short prefix shown before a formatted amount, e.g. "$" or "IRR". */
    val symbol: String,
    /** Digits after the decimal point when formatting amounts in this currency. */
    val minorUnitDigits: Int,
) {
  IRR("IRR", "IRR", 2),
  USD("USD", "$", 2),
  EUR("EUR", "€", 2),
  GBP("GBP", "£", 2),
  AED("AED", "AED", 2),
  TRY("TRY", "₺", 2),
  CHF("CHF", "CHF", 2),
  CAD("CAD", "$", 2),
  AUD("AUD", "$", 2),
  JPY("JPY", "¥", 0),
  CNY("CNY", "¥", 2),
  INR("INR", "₹", 2),
  SAR("SAR", "SAR", 2);

  /**
   * Formats [amount] with this currency's symbol and minor-unit digits, e.g.
   * `Currency.IRR.format(10.0)` is `"IRR10.00"` and `Currency.JPY.format(10.0)` is `"¥10"`.
   */
  fun format(amount: Double): String {
    val factor = 10.0.pow(minorUnitDigits)
    val rounded = round(amount * factor) / factor
    val parts = rounded.toString().split('.')
    val whole = parts[0]
    val decimal =
        when {
          minorUnitDigits == 0 -> ""
          parts.size > 1 ->
              "." + parts[1].padEnd(minorUnitDigits, '0').substring(0, minorUnitDigits)
          else -> "." + "0".repeat(minorUnitDigits)
        }
    return "$symbol$whole$decimal"
  }

  companion object {
    /** The currency every group uses today. */
    val default: Currency = IRR

    /** Finds a currency by ISO code, ignoring case, or returns null for unknown codes. */
    fun fromCode(code: String): Currency? = entries.find { it.code.equals(code, ignoreCase = true) }
  }
}
