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

    /** Returns canonical form: mantissa in [1, 1000) or 0. */
    fun normalized(): BigNumber {
        if (mantissa == 0.0 || mantissa.isNaN()) return ZERO

        var m = mantissa
        var e = exponent

        // handle NaN/inf
        if (m.isInfinite()) return ZERO

        while (abs(m) >= 1000.0) {
            m /= 1000.0
            e += 3
        }
        while (abs(m) < 1.0) {
            m *= 1000.0
            e -= 3
        }
        return BigNumber(m, e)
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

    /** Pretty string with suffixes for small numbers, e-notation for big */
    fun toPrettyString(): String {
        val n = normalized()
        if (n.isZero()) return "0"
        if (n.exponent < 6) {
            val value = n.toDouble()
            return String.format(Locale.US, "%,.2f", value)
        }
        return n.toEngineeringString()
    }
}