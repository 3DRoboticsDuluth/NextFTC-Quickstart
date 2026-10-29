# Calibration curves

`3drdNextFTC` provides season-neutral, stateless curve evaluators under
`org.firstinspires.ftc.threedrd.nextftc.control.curves` (REQ-PLT-057).
They implement `Curve.calculate(x)` and return a number in the units established
by the calibration. They do not command hardware or fit coefficients from data.

## Analytic models

| Class | Equation | Coefficients and domain |
|---|---|---|
| `PolynomialCurve(c0, c1, ...)` | `c0 + c1*x + c2*x^2 + ...` | At least one coefficient; any finite x. One class covers constant, linear, quadratic, cubic and higher degrees. |
| `LogisticCurve(a, b, c, d, g = 1)` | `d + (a-d)/(1+(x/c)^b)^g` | x >= 0, c > 0, g > 0. g=1 is 4PL; adjustable g supplies 5PL asymmetry. |
| `ExponentialCurve(a, b, c = 0)` | `a*exp(b*x) + c` | Any finite x with a finite result. |
| `LogarithmicCurve(a, b = 0)` | `a*ln(x) + b` | Natural logarithm; x > 0. |
| `PowerCurve(a, b, c = 0)` | `a*x^b + c` | Kotlin real Double.pow semantics; negative x requires an exponent that yields a finite real result. Zero with a negative exponent is invalid. |
| `GaussianCurve(a, b, c, d = 0)` | `a*exp(-0.5*((x-b)/c)^2) + d` | a is amplitude, b center, c positive standard deviation and d baseline. |

All built-in models validate finite input and coefficients on each call, including
after live edits. Invalid domains, empty polynomials, invalid scales/asymmetry and
nonfinite final results throw IllegalArgumentException. Internal logistic powers
may saturate to infinity while still yielding a valid finite asymptote. These
evaluators do not clamp outputs or distances and do not swallow invalid settings.

For logistic b > 0, a is the response at zero and d is the limiting response at
large x. With g=1, c is the halfway-response input. With g != 1, c is no longer
the halfway-response input. A negative b reverses the endpoint roles. Both rising
and falling responses can be expressed. Gaussian c is a standard deviation, not
the width parameter in equations that omit the factor 0.5.

## Fit elsewhere, evaluate on the robot

Use measured calibration pairs and an external fitting tool such as
[MyCurveFit](https://mycurvefit.com/) to estimate the coefficients. The
[publisher's feature list](https://www.myassays.com/mycurvefit-launch.html) spans
analytic models, splines and custom fits. This library is an initial analytic
subset, not a mirror of the current website menu or a regression engine.
Spline construction, interpolation tables and expression parsing are separate
future work.

Check the exported equation before copying numbers. Match coefficient order,
logarithm base, units, offsets, width conventions and logistic parameterization.
For an exported `a*x^2 + b*x + c`, construct `PolynomialCurve(c, b, a)`.
Store the measurements and validated input range in the team's documentation.
The consuming subsystem decides what to do outside that range and how to bound a
hardware command; a mathematically finite extrapolation is not calibration evidence.

## Live configuration and interchangeable models

Expose concrete model types as ordinary mutable fields under `@Configurable`:

```kotlin
var FIT = LogisticCurve(a = 0.2, b = 2.0, c = 50.0, d = 0.8)
val evaluate: (Double) -> Double = { FIT.calculate(it) }
```

Each instance owns its coefficients. Panels can edit scalar coefficients and
PolynomialCurve.coefficients array elements. Edits affect the next calculation;
no initialize/reset step is needed. The callback above also reads a replacement
FIT object on its next invocation; a bound `FIT::calculate` keeps the original
object instead. Keep the field's concrete type: the installed Panels scanner
inspects declared fields, so `var FIT: Curve = ...` hides the concrete coefficients.
Model types are selected in code, not through a live model-selector menu.
Keep the polynomial array length fixed while editing in Panels; rescan its
configuration fields after changing polynomial degree.

A consumer accepts a `Curve` or `(Double) -> Double` to avoid depending on any
particular formula. Changing the concrete fit declaration and callback allows a
different family without changing the consumer's calculation. Custom compiled
functions can implement `Curve { x -> ... }`; custom implementations own their
input-domain and result validation.

CurveTests verify documented equations, live scalar/array edits and replacement,
4PL/5PL behavior, endpoint direction, known calibration values, instance isolation,
invalid domains, nonfinite parameters and overflow. Robot consumers add tests for
their chosen units, Panels paths, callback replacement and command behavior.
