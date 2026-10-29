package org.firstinspires.ftc.threedrd.nextftc.control.curves

import kotlin.math.*
import org.junit.Assert.*
import org.junit.Test

class CurveTests {
    @Test
    fun polynomialCoversConstantLinearQuadraticAndCubicWithAscendingCoefficients() {
        assertEquals(7.0, PolynomialCurve(7.0).calculate(-10.0), 0.0)
        assertEquals(7.0, PolynomialCurve(1.0, 2.0).calculate(3.0), 0.0)
        assertEquals(17.0, PolynomialCurve(1.0, 2.0, 3.0).calculate(2.0), 0.0)
        assertEquals(-23.0, PolynomialCurve(1.0, 2.0, 3.0, 4.0).calculate(-2.0), 0.0)
        val curve = PolynomialCurve(1.0, 2.0)
        curve.coefficients[0] = 3.0
        assertEquals(7.0, curve.calculate(2.0), 0.0)
        curve.coefficients = doubleArrayOf(5.0)
        assertEquals(5.0, curve.calculate(2.0), 0.0)
    }

    @Test
    fun logisticSupportsFourAndFiveParametersIncreasingAndDecreasingCurves() {
        val curve = LogisticCurve(2.0, 2.0, 10.0, 6.0)
        assertEquals(2.0, curve.calculate(0.0), 0.0)
        assertEquals(4.0, curve.calculate(10.0), 0.0)
        assertEquals(5.2, curve.calculate(20.0), 0.0000001)
        assertEquals(6.0, curve.calculate(Double.MAX_VALUE), 0.0)
        curve.g = 2.0
        assertEquals(5.0, curve.calculate(10.0), 0.0)
        curve.a = 6.0
        curve.d = 2.0
        curve.g = 1.0
        assertEquals(6.0, curve.calculate(0.0), 0.0)
        assertEquals(4.0, curve.calculate(10.0), 0.0)
        curve.b = -2.0
        assertEquals(2.0, curve.calculate(0.0), 0.0)
        curve.c = 20.0
        assertEquals(4.0, curve.calculate(20.0), 0.0)
    }

    @Test
    fun logisticMatchesKnownCalibrationAndAllCoefficientsRemainLive() {
        val curve = LogisticCurve(0.2797805, 3.400669, 177.5942, 1.064487)
        assertEquals(0.2797805, curve.calculate(0.0), 0.0000001)
        assertEquals(0.29017935350557345, curve.calculate(50.0), 0.0000001)
        assertEquals((0.2797805 + 1.064487) / 2, curve.calculate(177.5942), 0.0000001)
        assertEquals(1.064487, curve.calculate(1e100), 0.0000001)
    }

    @Test
    fun exponentialLogarithmicPowerAndGaussianFollowTheirDocumentedEquations() {
        val exponential = ExponentialCurve(2.0, 1.0)
        assertEquals(2 * E, exponential.calculate(1.0), 0.0000001)
        exponential.a = 3.0; exponential.b = -1.0; exponential.c = 4.0
        assertEquals(3 / E + 4, exponential.calculate(1.0), 0.0000001)
        val logarithmic = LogarithmicCurve(2.0)
        assertEquals(2.0, logarithmic.calculate(E), 0.0000001)
        logarithmic.a = 3.0; logarithmic.b = 4.0
        assertEquals(7.0, logarithmic.calculate(E), 0.0000001)
        val power = PowerCurve(2.0, 3.0)
        assertEquals(-16.0, power.calculate(-2.0), 0.0)
        power.a = 3.0; power.b = 2.0; power.c = 4.0
        assertEquals(16.0, power.calculate(-2.0), 0.0)
        val gaussian = GaussianCurve(2.0, 3.0, 4.0)
        assertEquals(2.0, gaussian.calculate(3.0), 0.0)
        assertEquals(2 * exp(-0.5), gaussian.calculate(7.0), 0.0000001)
        assertEquals(gaussian.calculate(-1.0), gaussian.calculate(7.0), 0.0)
        gaussian.a = 3.0; gaussian.b = 4.0; gaussian.c = 2.0; gaussian.d = 5.0
        assertEquals(8.0, gaussian.calculate(4.0), 0.0)
        assertEquals(3 * exp(-0.5) + 5, gaussian.calculate(6.0), 0.0000001)
    }

    @Test
    fun invalidDomainsLiveParametersNonfiniteInputsAndOverflowFailClearly() {
        val curves = listOf<Curve>(PolynomialCurve(1.0), LogisticCurve(1.0, 2.0, 3.0, 4.0),
            ExponentialCurve(1.0, 1.0), LogarithmicCurve(1.0), PowerCurve(1.0, 2.0), GaussianCurve(1.0, 0.0, 1.0))
        for (curve in curves) {
            for (x in listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)) {
                assertThrows(IllegalArgumentException::class.java) { curve.calculate(x) }
            }
        }
        for (curve in listOf<Curve>(PolynomialCurve(), PolynomialCurve(Double.NaN),
                LogisticCurve(1.0, 2.0, 0.0, 4.0), LogisticCurve(1.0, 2.0, 3.0, 4.0, 0.0),
                GaussianCurve(1.0, 0.0, 0.0), ExponentialCurve(1.0, Double.NaN))) {
            assertThrows(IllegalArgumentException::class.java) { curve.calculate(1.0) }
        }
        assertThrows(IllegalArgumentException::class.java) { curves[1].calculate(-1.0) }
        assertThrows(IllegalArgumentException::class.java) { curves[3].calculate(0.0) }
        assertThrows(IllegalArgumentException::class.java) { curves[3].calculate(-1.0) }
        assertThrows(IllegalArgumentException::class.java) { PowerCurve(1.0, 0.5).calculate(-1.0) }
        assertThrows(IllegalArgumentException::class.java) { PowerCurve(1.0, -1.0).calculate(0.0) }
        assertThrows(IllegalArgumentException::class.java) { ExponentialCurve(1.0, 1.0).calculate(1000.0) }
        assertThrows(IllegalArgumentException::class.java) { PolynomialCurve(0.0, Double.MAX_VALUE).calculate(2.0) }
        val live = GaussianCurve(1.0, 0.0, 1.0)
        live.c = -1.0
        assertThrows(IllegalArgumentException::class.java) { live.calculate(1.0) }
    }

    @Test
    fun customCurveCanUseACompiledKotlinFunction() {
        val curve = Curve { x -> x * 2 + 1 }
        assertEquals(7.0, curve.calculate(3.0), 0.0)
        val other = LogisticCurve(0.0, 1.0, 1.0, 1.0)
        val first = LogisticCurve(0.0, 1.0, 1.0, 1.0)
        first.a = 2.0
        assertEquals(1.5, first.calculate(1.0), 0.0)
        assertEquals(0.5, other.calculate(1.0), 0.0)
    }
}
