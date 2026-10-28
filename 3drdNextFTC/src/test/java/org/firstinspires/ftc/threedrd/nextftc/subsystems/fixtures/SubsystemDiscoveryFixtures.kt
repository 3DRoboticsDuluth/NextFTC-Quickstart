package org.firstinspires.ftc.threedrd.nextftc.subsystems.fixtures

import org.firstinspires.ftc.threedrd.nextftc.subsystems.*

object DiscoveredSubsystem : Subsystem()
abstract class AbstractSubsystem : Subsystem()
class ConstructedSubsystem : Subsystem()
class NotASubsystem

@Discovery(owned = true)
class OwnedSubsystem : Subsystem()

@Discovery(disabled = true)
object DisabledDiscovered : Subsystem() {
    init { error("Disabled singleton must not be initialized") }
}

@Discovery(mode = DiscoveryMode.AUTO)
object AutoDiscovered : Subsystem()

@Discovery(mode = DiscoveryMode.TELEOP)
object TeleopDiscovered : Subsystem()

@Discovery
object BothDiscovered : Subsystem()

class NestedSubsystem {
    object Instance : Subsystem()
}

class InvalidInstanceSubsystem : Subsystem() {
    companion object {
        @JvmField
        val INSTANCE = "Not a subsystem"
    }
}
