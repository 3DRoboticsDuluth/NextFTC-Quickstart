# Add a motor and sensor subsystem

Use the [servo example](subsystem.md) for a positional mechanism. This example
shows a powered roller and a digital sensor. Replace names, direction, active-low
sensor interpretation, and power with your robot's documented choices.

```kotlin
import com.bylazar.configurables.annotations.*
import com.qualcomm.robotcore.hardware.*
import com.qualcomm.robotcore.hardware.DigitalChannel.Mode.INPUT
import dev.nextftc.ftc.Gamepads.gamepad2
import org.firstinspires.ftc.threedrd.nextftc.hardware.*
import org.firstinspires.ftc.threedrd.nextftc.subsystems.*

@Configurable
object Roller : Subsystem() {
    var POWER = 0.3
    var target = 0.0

    val motor = MotorEx("roller")
    val sensor by Device("rollerSensor", DigitalChannel::class.java) { mode = INPUT }
    val occupied get() = !sensor.state

    val run by instant { target = POWER }
    val stopRoller by instant { target = 0.0 }

    override fun initialize() { target = 0.0; motor.power = 0.0 }
    override fun controls() {
        gamepad2.a whenBecomesTrue run
        gamepad2.a whenBecomesFalse stopRoller
    }
    override fun periodic() {
        motor.update { power = target }
        tel.info("Occupied", occupied)
    }
    override fun stop() { target = 0.0; motor.power = 0.0 }
}
```

Place the object under `org.firstinspires.ftc.teamcode.subsystems` so discovery
initializes its hardware. `Device` uses name-first construction; `by` exposes
the digital channel. The motor retains its enhanced wrapper. Controls become
active only after Teleop Start. The sensor reports current occupancy here; it
does not automatically stop the roller unless that is a documented requirement.

Before copying this into a robot, add `RollerTests` in the matching subsystem test
package. Assert initialization at zero, press/release target changes, periodic
motor output and telemetry, both sensor states, failed hardware isolation, and
immediate zero output on Stop. Verify on hardware that sensor polarity and motor
direction match the documentation, then record the result in [Our robot](../robot/validation.md).
