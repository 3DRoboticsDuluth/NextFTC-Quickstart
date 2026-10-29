package org.firstinspires.ftc.threedrd.nextftc.control.curves

import kotlin.math.*

fun interface Curve {
    fun calculate(x: Double): Double
}

class PolynomialCurve(vararg coefficients: Double) : Curve {
    var coefficients = coefficients

    override fun calculate(x: Double): Double {
        val terms = coefficients.copyOf()
        require(terms.isNotEmpty()) { "A polynomial needs at least one coefficient" }
        finite(x, *terms)
        var value = 0.0
        for (coefficient in terms.reversed()) value = value * x + coefficient
        return result(value)
    }
}

class LogisticCurve(var a: Double, var b: Double, var c: Double, var d: Double, var g: Double = 1.0) : Curve {
    override fun calculate(x: Double): Double {
        finite(x, a, b, c, d, g)
        require(x >= 0.0) { "Logistic input must be nonnegative" }
        require(c > 0.0) { "Logistic scale c must be positive" }
        require(g > 0.0) { "Logistic asymmetry g must be positive" }
        return result(d + (a - d) / (1 + (x / c).pow(b)).pow(g))
    }
}

class ExponentialCurve(var a: Double, var b: Double, var c: Double = 0.0) : Curve {
    override fun calculate(x: Double): Double {
        finite(x, a, b, c)
        return result(a * exp(b * x) + c)
    }
}

class LogarithmicCurve(var a: Double, var b: Double = 0.0) : Curve {
    override fun calculate(x: Double): Double {
        finite(x, a, b)
        require(x > 0.0) { "Logarithmic input must be positive" }
        return result(a * ln(x) + b)
    }
}

class PowerCurve(var a: Double, var b: Double, var c: Double = 0.0) : Curve {
    override fun calculate(x: Double): Double {
        finite(x, a, b, c)
        return result(a * x.pow(b) + c)
    }
}

class GaussianCurve(var a: Double, var b: Double, var c: Double, var d: Double = 0.0) : Curve {
    override fun calculate(x: Double): Double {
        finite(x, a, b, c, d)
        require(c > 0.0) { "Gaussian standard deviation c must be positive" }
        val z = (x - b) / c
        return result(a * exp(-0.5 * z * z) + d)
    }
}

private fun finite(vararg values: Double) {
    require(values.all { it.isFinite() }) { "Curve input and coefficients must be finite" }
}

private fun result(value: Double): Double {
    require(value.isFinite()) { "Curve result is not finite; check the input domain and coefficient range" }
    return value
}
