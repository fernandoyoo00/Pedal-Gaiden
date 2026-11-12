"""Ponte local BLE Cycling Speed and Cadence -> UDP do Pedal Gaiden."""

import argparse
import asyncio
import logging
import socket
import struct
import time

CSC_SERVICE = "00001816-0000-1000-8000-00805f9b34fb"
CSC_MEASUREMENT = "00002a5b-0000-1000-8000-00805f9b34fb"
CSC_FEATURE = "00002a5c-0000-1000-8000-00805f9b34fb"
STOP_TIMEOUT = 3.0
LOG = logging.getLogger("cadencia")


class Cadence:
    def __init__(self):
        self.previous = None
        self.last_rotation = None
        self.rpm = 0.0

    def measurement(self, data, now):
        if not data:
            raise ValueError("Medicao CSC vazia")
        flags = data[0]
        if flags & ~3:
            raise ValueError("Flags CSC desconhecidas")
        expected = 1 + (6 if flags & 1 else 0) + (4 if flags & 2 else 0)
        if len(data) != expected:
            raise ValueError("Tamanho de medicao CSC invalido")
        if not flags & 2:
            return False
        offset = 1 + (6 if flags & 1 else 0)
        revolutions, ticks = struct.unpack_from("<HH", data, offset)
        current = (revolutions, ticks)
        if self.previous is None or (
            self.last_rotation is not None and now - self.last_rotation > STOP_TIMEOUT
        ):
            self.previous = current
            self.last_rotation = now
            self.rpm = 0.0
            return True
        delta_revs = (revolutions - self.previous[0]) & 0xFFFF
        delta_ticks = (ticks - self.previous[1]) & 0xFFFF
        if delta_revs:
            self.previous = current
            if not delta_ticks:
                self.rpm = 0.0
                raise ValueError("Rotacoes recebidas sem intervalo de tempo")
            rpm = 60.0 * 1024 * delta_revs / delta_ticks
            if not 0 < rpm <= 160:
                self.rpm = 0.0
                raise ValueError("Cadencia fora do intervalo aceito pelo jogo (0-160 RPM)")
            self.rpm = rpm
            self.last_rotation = now
        return True

    def value(self, now):
        if self.last_rotation is None or now - self.last_rotation >= STOP_TIMEOUT:
            return 0.0
        return self.rpm


async def run(args):
    from bleak import BleakClient, BleakScanner

    if args.command == "scan":
        devices = await BleakScanner.discover(timeout=10, return_adv=True)
        found = False
        for device, advertisement in devices.values():
            name = advertisement.local_name or device.name or "(sem nome)"
            services = [uuid.lower() for uuid in advertisement.service_uuids]
            if "xoss" in name.lower() or CSC_SERVICE in services:
                found = True
                print(f"{name} | ID: {device.address} | RSSI: {advertisement.rssi}")
                print(f"  Servicos anunciados: {', '.join(services) or '(nenhum)'}")
        if not found:
            print("Nenhum XOSS/CSC encontrado. Pedale para acordar o sensor e desconecte o app XOSS.")
        return

    device = await BleakScanner.find_device_by_address(args.device, timeout=15)
    if device is None:
        raise RuntimeError("Sensor nao encontrado. Execute scan novamente e confirme o ID.")
    disconnected = asyncio.Event()
    async with BleakClient(device, disconnected_callback=lambda _: disconnected.set()) as client:
        service = client.services.get_service(CSC_SERVICE)
        if service is None:
            raise RuntimeError("O dispositivo nao oferece o servico padrao CSC (1816).")
        characteristic = service.get_characteristic(CSC_MEASUREMENT)
        if characteristic is None or "notify" not in characteristic.properties:
            raise RuntimeError("O sensor nao oferece notificacoes CSC Measurement (2A5B).")
        feature = service.get_characteristic(CSC_FEATURE)
        if feature is not None and "read" in feature.properties:
            raw = await client.read_gatt_char(feature)
            if len(raw) != 2:
                raise RuntimeError("Resposta CSC Feature invalida.")
            bits = int.from_bytes(raw, "little")
            print(f"CSC confirmado. Roda: {bool(bits & 1)} | Pedivela/cadencia: {bool(bits & 2)}")
            if not bits & 2:
                raise RuntimeError("Sensor em modo velocidade. Configure Cadence no app XOSS.")
        if args.command == "inspect":
            print("Caracteristica 2A5B encontrada. Use bridge e pedale para confirmar dados do pedivela.")
            return

        cadence = Cadence()
        crank_seen = False

        def notification(_, data):
            nonlocal crank_seen
            try:
                if cadence.measurement(data, time.monotonic()):
                    if not crank_seen:
                        LOG.info("Dados de cadencia confirmados.")
                    crank_seen = True
                else:
                    LOG.warning("Medicao sem pedivela: confirme o modo Cadence, nao Speed.")
            except ValueError as error:
                cadence.rpm = 0.0
                LOG.error("Medicao rejeitada: %s", error)

        with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as udp:
            destination = ("127.0.0.1", 5055)
            try:
                await client.start_notify(characteristic, notification)
                LOG.info("Conectado. Enviando para 127.0.0.1:5055. Pedale; Ctrl+C encerra.")
                last_report = None
                started = time.monotonic()
                while not disconnected.is_set():
                    now = time.monotonic()
                    rpm = cadence.value(now)
                    udp.sendto(f"RPM:{rpm:.2f}".encode("ascii"), destination)
                    report = round(rpm)
                    if report != last_report:
                        LOG.info("Cadencia: %s RPM", report)
                        last_report = report
                    if not crank_seen and now - started > 15:
                        raise RuntimeError("Sem dados de pedivela por 15s. Pedale e confirme o modo Cadence.")
                    await asyncio.sleep(.25)
                raise RuntimeError("Sensor desconectado. Execute bridge novamente para reconectar.")
            finally:
                udp.sendto(b"RPM:0", destination)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest="command", required=True)
    commands.add_parser("scan", help="Localiza candidatos XOSS/CSC sem conectar")
    for command in ("inspect", "bridge"):
        subparser = commands.add_parser(command)
        subparser.add_argument("--device", required=True, help="ID retornado por scan (UUID no macOS)")
    args = parser.parse_args()
    logging.basicConfig(level=logging.INFO, format="%(levelname)s: %(message)s")
    try:
        asyncio.run(run(args))
    except KeyboardInterrupt:
        LOG.info("Conexao encerrada.")
    except ImportError as error:
        parser.exit(1, f"Dependencia ausente: {error}. Instale tools/requirements.txt em um ambiente virtual.\n")
    except Exception as error:
        LOG.error("Falha BLE: %s", error)
        parser.exit(1, "Verifique Bluetooth, permissao do terminal e se o app XOSS esta desconectado.\n")


if __name__ == "__main__":
    main()
