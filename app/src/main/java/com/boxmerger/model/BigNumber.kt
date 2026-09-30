package com.boxmerger.model

import java.util.Locale
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow

data class BigNumber(val mantissa: Double, val exponent: Int) : Comparable<BigNumber> {

    constructor(value: Double) : this(
        if (value == 0.0) 0.0 else {
            val exp = log10(abs(value)).toInt()
            val engExp = (exp / 3) * 3
            value / 10.0.pow(engExp)
        },
        if (value == 0.0) 0 else {
            (log10(abs(value)).toInt() / 3) * 3
        }
    )

    fun normalized(): BigNumber {
        if (mantissa == 0.0) return BigNumber(0.0, 0)
        var m = mantissa
        var e = exponent
        while (abs(m) >= 1000.0) {
            m /= 1000.0
            e += 3
        }
        while (abs(m) < 1.0 && m != 0.0) {
            m *= 1000.0
            e -= 3
        }
        return BigNumber(m, e)
    }

    operator fun plus(other: BigNumber): BigNumber {
        val n1 = this.normalized()
        val n2 = other.normalized()
        if (n1.mantissa == 0.0) return n2
        if (n2.mantissa == 0.0) return n1

        val diff = n1.exponent - n2.exponent
        return when {
            diff == 0 -> BigNumber(n1.mantissa + n2.mantissa, n1.exponent).normalized()
            diff > 0 -> {
                if (diff > 300) return n1
                val adjustedOther = n2.mantissa / 10.0.pow(diff)
                BigNumber(n1.mantissa + adjustedOther, n1.exponent).normalized()
            }
            else -> {
                if (diff < -300) return n2
                val adjustedThis = n1.mantissa / 10.0.pow(-diff)
                BigNumber(adjustedThis + n2.mantissa, n2.exponent).normalized()
            }
        }
    }

    operator fun minus(other: BigNumber): BigNumber {
        return this + (other * -1.0)
    }

    operator fun times(other: BigNumber): BigNumber {
        val n1 = this.normalized()
        val n2 = other.normalized()
        if (n1.mantissa == 0.0 || n2.mantissa == 0.0) return ZERO
        return BigNumber(n1.mantissa * n2.mantissa, n1.exponent + n2.exponent).normalized()
    }

    operator fun times(scalar: Double): BigNumber {
        return this * BigNumber(scalar)
    }

    override fun compareTo(other: BigNumber): Int {
        val n1 = this.normalized()
        val n2 = other.normalized()
        if (n1.mantissa == 0.0 && n2.mantissa == 0.0) return 0
        if (n1.mantissa == 0.0) return -1
        if (n2.mantissa == 0.0) return 1

        if (n1.exponent != n2.exponent) {
            return n1.exponent.compareTo(n2.exponent)
        }
        return n1.mantissa.compareTo(n2.mantissa)
    }

    fun toEngineeringString(): String {
        val n = this.normalized()
        if (n.mantissa == 0.0) return "0.0"
        if (n.exponent == 0) {
            return String.format(Locale.US, "%.1f", n.mantissa)
        }
        return String.format(Locale.US, "%.1fe%d", n.mantissa, n.exponent)
    }

    companion object {
        val ZERO = BigNumber(0.0, 0)
        val ONE = BigNumber(1.0, 0)
    }
}
