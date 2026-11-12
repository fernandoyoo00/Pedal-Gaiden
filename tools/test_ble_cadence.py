import struct
import unittest

from ble_cadence import Cadence


def crank(revolutions, ticks):
    return b"\x02" + struct.pack("<HH", revolutions, ticks)


class CadenceTest(unittest.TestCase):
    def test_rpm_and_stop(self):
        cadence = Cadence()
        self.assertTrue(cadence.measurement(crank(10, 1024), 0))
        self.assertEqual(cadence.value(0), 0)
        cadence.measurement(crank(11, 2048), 1)
        self.assertEqual(cadence.value(1), 60)
        cadence.measurement(crank(11, 2048), 3)
        self.assertEqual(cadence.value(4), 0)
        cadence.measurement(crank(12, 3072), 5)
        self.assertEqual(cadence.value(5), 0)
        cadence.measurement(crank(13, 4096), 6)
        self.assertEqual(cadence.value(6), 60)

    def test_rollover(self):
        cadence = Cadence()
        cadence.measurement(crank(65535, 65024), 0)
        cadence.measurement(crank(0, 512), 1)
        self.assertEqual(cadence.value(1), 60)

    def test_wheel_and_crank(self):
        cadence = Cadence()
        for i in range(2):
            packet = b"\x03" + struct.pack("<IH", 100, 300) + struct.pack("<HH", i, i * 1024)
            self.assertTrue(cadence.measurement(packet, i))
        self.assertEqual(cadence.value(1), 60)
        self.assertFalse(cadence.measurement(b"\x01" + struct.pack("<IH", 1, 1), 2))

    def test_invalid(self):
        for data in (b"", b"\x02", b"\x04", crank(1, 1) + b"\x00"):
            with self.assertRaises(ValueError):
                Cadence().measurement(data, 0)
        cadence = Cadence()
        cadence.measurement(crank(0, 0), 0)
        with self.assertRaises(ValueError):
            cadence.measurement(crank(100, 1), 1)


if __name__ == "__main__":
    unittest.main()
