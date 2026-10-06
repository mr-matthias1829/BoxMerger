// model/BigNumber.kt
package com.boxmerger.model

import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Immutable big number: value = mantissa * 10^exponent
 * Mantissa is kept in [1.0, 1000.0) for non-zero values (engineering notation).
 * This supports numbers up to Double.MAX_VALUE * 10^Int.MAX_VALUE.
 */
data class BigNumber(val mantissa: Double, val exponent: Int) : Comparable<BigNumber> {

    init {
        // no validation here; use normalized() to get canonical form
    }

    companion object {
        val ZERO = BigNumber(0.0, 0)
        val ONE = BigNumber(1.0, 0)
        val TEN = BigNumber(10.0, 0)

        /** Safe constructor from Double. Handles 0, negatives, NaN, Infinity. */
        fun of(value: Double): BigNumber {
            if (value.isNaN() || value.isInfinite()) return ZERO
            if (value == 0.0) return ZERO

            val sign = if (value < 0) -1.0 else 1.0
            val absValue = abs(value)
            val log = log10(absValue)
            val exp = floor(log).toInt()
            val engExp = (exp / 3) * 3
            val m = sign * absValue / 10.0.pow(engExp.toDouble())
            return BigNumber(m, engExp).normalized()
        }

        /** Construct from mantissa/exponent directly, normalizing. */
        fun of(mantissa: Double, exponent: Int): BigNumber {
            return BigNumber(mantissa, exponent).normalized()
        }
    }

    fun isZero(): Boolean = mantissa == 0.0

    /** Returns canonical form: mantissa in [1.0, 1000.0) and exponent as a multiple of 3, or ZERO. */
    fun normalized(): BigNumber {
        if (mantissa == 0.0 || mantissa.isNaN() || mantissa.isInfinite()) return ZERO

        val sign = if (mantissa < 0) -1.0 else 1.0
        val absM = abs(mantissa)

        val totalLog = log10(absM) + exponent
        if (totalLog.isNaN() || totalLog.isInfinite()) return ZERO

        val exp = floor(totalLog).toInt()
        val engExp = (exp / 3) * 3
        var newMantissa = sign * 10.0.pow(totalLog - engExp)

        // Handle floating point rounding bounds
        if (abs(newMantissa) >= 1000.0) {
            newMantissa /= 1000.0
            return BigNumber(newMantissa, engExp + 3)
        }
        if (abs(newMantissa) < 1.0) {
            newMantissa *= 1000.0
            return BigNumber(newMantissa, engExp - 3)
        }

        return BigNumber(newMantissa, engExp)
    }

    operator fun plus(other: BigNumber): BigNumber {
        val a = this.normalized()
        val b = other.normalized()
        if (a.isZero()) return b
        if (b.isZero()) return a

        // Align exponents
        val diff = a.exponent - b.exponent
        if (diff == 0) {
            return BigNumber(a.mantissa + b.mantissa, a.exponent).normalized()
        }

        // If difference is huge, the smaller number is negligible
        if (diff > 300) return a
        if (diff < -300) return b

        if (diff > 0) {
            val adjustedB = b.mantissa / 10.0.pow(diff.toDouble())
            return BigNumber(a.mantissa + adjustedB, a.exponent).normalized()
        } else {
            val adjustedA = a.mantissa / 10.0.pow(-diff.toDouble())
            return BigNumber(adjustedA + b.mantissa, b.exponent).normalized()
        }
    }

    operator fun minus(other: BigNumber): BigNumber {
        return this + (other * -1.0)
    }

    operator fun times(other: BigNumber): BigNumber {
        val a = this.normalized()
        val b = other.normalized()
        if (a.isZero() || b.isZero()) return ZERO
        return BigNumber(a.mantissa * b.mantissa, a.exponent + b.exponent).normalized()
    }

    operator fun times(scalar: Double): BigNumber {
        if (scalar == 0.0) return ZERO
        val a = this.normalized()
        if (a.isZero()) return ZERO
        return BigNumber(a.mantissa * scalar, a.exponent).normalized()
    }

    operator fun div(other: BigNumber): BigNumber {
        val a = this.normalized()
        val b = other.normalized()
        if (b.isZero()) return ZERO
        if (a.isZero()) return ZERO
        return BigNumber(a.mantissa / b.mantissa, a.exponent - b.exponent).normalized()
    }

    operator fun div(scalar: Double): BigNumber {
        if (scalar == 0.0) return ZERO
        return this * (1.0 / scalar)
    }

    override fun compareTo(other: BigNumber): Int {
        val a = this.normalized()
        val b = other.normalized()

        val aZero = a.isZero()
        val bZero = b.isZero()
        if (aZero && bZero) return 0
        if (aZero) return -1
        if (bZero) return 1

        // Handle sign
        val aNeg = a.mantissa < 0
        val bNeg = b.mantissa < 0
        if (aNeg != bNeg) return if (aNeg) -1 else 1

        // Compare magnitudes
        if (a.exponent != b.exponent) {
            return a.exponent.compareTo(b.exponent)
        }
        return a.mantissa.compareTo(b.mantissa)
    }

    /** Returns this^p as BigNumber. p can be any double. */
    fun pow(p: Double): BigNumber {
        if (isZero()) return ZERO
        if (p == 0.0) return ONE

        val log10Value = log10(abs(mantissa)) + exponent
        val newLog10 = log10Value * p
        val newExp = floor(newLog10).toInt()
        val newMantissa = 10.0.pow(newLog10 - newExp)
        return BigNumber(newMantissa, newExp).normalized()
    }

    /** Square root */
    fun sqrt(): BigNumber = pow(0.5)

    /** Natural log (for debug) */
    fun log10(): Double {
        if (isZero()) return Double.NEGATIVE_INFINITY
        return log10(abs(mantissa)) + exponent
    }

    /** Convert to Double. Warning: may be Infinity for huge exponents. */
    fun toDouble(): Double {
        if (isZero()) return 0.0
        return mantissa * 10.0.pow(exponent.toDouble())
    }

    /**
     * Formats the number with a suffix (K, M, B, T, Qa, Qi, ...) for readability.
     * Falls back to scientific notation for extremely large values.
     * Decimals under 0.0001 use scientific notation; otherwise plain decimal.
     */
    fun toPrettyString(): String {
        val n = normalized()
        if (n.isZero()) return "0"

        val absVal = abs(n.toDouble())

        // Small decimals: show plain decimal unless extremely small
        if (absVal < 1.0) {
            if (absVal < 0.0001) {
                // tiny -> scientific
                return n.toEngineeringString()
            }
            return formatSmallDecimal(n)
        }

        // 1.0 to 999.999 -> just show the number
        if (absVal < 1000.0) {
            return formatWithCommas(n.toDouble())
        }

        // Use suffix notation
        return toSuffixedString()
    }

    private fun formatSmallDecimal(n: BigNumber): String {
        // For values < 1, we want e.g. "0.02", "0.5", "0.123"
        val d = n.toDouble()
        return when {
            d == floor(d) -> String.format(Locale.US, "%.0f", d)
            abs(d) >= 0.01 -> String.format(Locale.US, "%.3f", d).trimEnd('0').trimEnd('.')
            else -> String.format(Locale.US, "%.4f", d).trimEnd('0').trimEnd('.')
        }
    }

    private fun formatWithCommas(value: Double): String {
        return when {
            value == floor(value) -> String.format(Locale.US, "%,.0f", value)
            else -> {
                // Show 2 decimals, trim trailing zeros
                val s = String.format(Locale.US, "%,.2f", value)
                if (s.endsWith(".00")) s.dropLast(3)
                else s
            }
        }
    }

    /**
     * Suffix notation: 1.23K, 4.56M, 7.89B, 1.23T, 4.56Qa, ...
     * Uses engineering exponent to pick suffix.
     */
    private fun toSuffixedString(): String {
        val n = normalized()
        if (n.isZero()) return "0"

        // Determine which suffix group we're in based on exponent.
        // exponent is always a multiple of 3 (engineering notation).
        // Group index = exponent / 3.
        // 0 -> "", 1 -> K, 2 -> M, 3 -> B, 4 -> T, 5 -> Qa, ...

        val suffixes = arrayOf(
            "", "K", "M", "B", "T", // 0, 3, 6, 9, 12, the main ones
            "Qa", "Qi", "Sx", "Sp", "Oc", "No", // 15, 18, 21, 24, 27, 30
            "Dc", // 33

            /* Scrapped to keep numbers readable.
            "Ud", "Dd", "Td", "Qad", "Qid", // 36, 39, 42, 45, 48
            "Sxd", "Spd", "Ocd", "Nod", "Vg", // 51, 54, 57, 60, 63
            "Uvg", "Dvg", "Tvg", "Qavg", "Qivg", // 66, 69, 72, 75, 78
            "Sxvg", "Spvg", "Ocvg", "Novg", "Tg", // 81, 84, 87, 90, 93
            "Utg", "Dtg", "Ttg", "Qatg", "Qitg", // 96, 99, 102, 105, 108
            "Sxtg", "Sptg", "Octg", "Notg" // 111, 114, 117, 120
             */
        )

        val groupIndex = n.exponent / 3
        if (groupIndex < 0 || groupIndex >= suffixes.size) {
            // too large for suffixes -> use scientific
            return toEngineeringString()
        }

        val suffix = suffixes[groupIndex]
        val m = n.mantissa

        // Format mantissa: 1-3 digits before decimal, 2 after, trim zeros
        val formatted = when {
            m == floor(m) -> String.format(Locale.US, "%.0f", m)
            else -> {
                val s = String.format(Locale.US, "%.2f", m)
                if (s.endsWith("0")) s.dropLast(1) else s
            }
        }

        return "$formatted$suffix"
    }

    /** Engineering string: 1.23e6, 1.23e9, etc. */
    fun toEngineeringString(): String {
        val n = normalized()
        if (n.isZero()) return "0"
        if (n.exponent == 0) {
            return if (n.mantissa == floor(n.mantissa)) {
                String.format(Locale.US, "%.0f", n.mantissa)
            } else {
                String.format(Locale.US, "%.2f", n.mantissa)
            }
        }
        return String.format(Locale.US, "%.2fe%d", n.mantissa, n.exponent)
    }

    /** Pretty string alias (kept for compatibility) */
    fun toDisplayString(): String = toPrettyString()

    override fun toString(): String = toPrettyString()
}