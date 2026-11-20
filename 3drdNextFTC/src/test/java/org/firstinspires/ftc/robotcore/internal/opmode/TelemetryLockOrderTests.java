package org.firstinspires.ftc.robotcore.internal.opmode;

import org.junit.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;

/** Exercises the actual dependency's TelemetryImpl, not a copy of its locking code. */
public class TelemetryLockOrderTests {
    private static class CycleDetected extends RuntimeException { }

    private static class Probe extends TelemetryImpl {
        final CountDownLatch updateHasLock = new CountDownLatch(1);
        final CountDownLatch loggerHasLock = new CountDownLatch(1);
        volatile boolean updateRequestedLogLock;
        volatile boolean loggerRequestedUpdateLock;

        Probe() {
            super(null);
            msTransmissionInterval = -1;
            actions.add(() -> {
                assertTrue(Thread.holdsLock(theLock));
                updateHasLock.countDown();
                await(loggerHasLock);
            });
            log = new LogImpl() {
                @Override Object getLock() {
                    if (Thread.holdsLock(theLock) && !Thread.holdsLock(Probe.this)) {
                        // The SDK updater is about to acquire B while holding A.
                        // The logger already owns B and is attempting A in the real SDK.
                        updateRequestedLogLock = true;
                        msTransmissionInterval = Integer.MAX_VALUE;
                        throw new CycleDetected(); // Unwind A rather than leave deadlocked test threads.
                    }
                    return super.getLock();
                }
            };
        }

        @Override protected boolean tryUpdate(UpdateReason reason) {
            if (reason == UpdateReason.LOG) {
                assertTrue(Thread.holdsLock(this));
                loggerRequestedUpdateLock = true;
                loggerHasLock.countDown();
            }
            return super.tryUpdate(reason);
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            assertTrue("Timed out coordinating SDK lock paths", latch.await(3, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }

    @Test public void sdkLogAddAndTelemetryUpdateRequestOppositeLocks() throws Exception {
        Probe telemetry = new Probe();
        AtomicReference<Throwable> updateFailure = new AtomicReference<>();
        AtomicReference<Throwable> logFailure = new AtomicReference<>();
        Thread updater = new Thread(() -> {
            try { telemetry.update(); } catch (Throwable t) { updateFailure.set(t); }
        }, "SDK-telemetry-update");
        Thread logger = new Thread(() -> {
            try { telemetry.log().add("Event"); } catch (Throwable t) { logFailure.set(t); }
        }, "OpMode-log-add");
        updater.setDaemon(true);
        logger.setDaemon(true);
        updater.start();
        await(telemetry.updateHasLock);
        logger.start();
        updater.join(4000);
        logger.join(4000);
        assertFalse("Updater must unwind the intercepted cycle", updater.isAlive());
        assertFalse("Logger must finish after the updater releases A", logger.isAlive());
        assertTrue(telemetry.updateRequestedLogLock);
        assertTrue(telemetry.loggerRequestedUpdateLock);
        assertTrue(updateFailure.get() instanceof CycleDetected);
        assertNull(logFailure.get());
    }
}
