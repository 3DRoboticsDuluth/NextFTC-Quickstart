import importlib.util
from pathlib import Path
import unittest

spec = importlib.util.spec_from_file_location("capture_hang", Path(__file__).with_name("capture-hang.py"))
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class HeartbeatTests(unittest.TestCase):
    def test_does_not_capture_before_first_live_heartbeat(self):
        self.assertFalse(module.Heartbeat().stalled(100))

    def test_healthy_heartbeat_moves_the_deadline(self):
        heartbeat = module.Heartbeat()
        heartbeat.observe(10)
        self.assertFalse(heartbeat.stalled(16.5))
        heartbeat.observe(15)
        self.assertFalse(heartbeat.stalled(20))

    def test_stall_captures_once_and_new_heartbeat_rearms(self):
        heartbeat = module.Heartbeat()
        heartbeat.observe(10)
        self.assertTrue(heartbeat.stalled(17))
        self.assertFalse(heartbeat.stalled(30))
        heartbeat.observe(31)
        self.assertTrue(heartbeat.stalled(38))


if __name__ == "__main__":
    unittest.main()
