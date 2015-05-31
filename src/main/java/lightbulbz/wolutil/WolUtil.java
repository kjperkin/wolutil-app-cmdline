package lightbulbz.wolutil;

import lightbulbz.net.MacAddress;
import lightbulbz.net.MacAddressFormatException;

import java.io.IOException;
import java.net.*;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class WolUtil
{
    private static final byte FF = (byte)0xff;
    private static final byte[] PAYLOAD_START = {FF, FF, FF, FF, FF, FF};
    private static final int WOL_PACKET_SIZE = 6 + 16*6;

    public static void main(String[] argv) {
        if (argv.length != 1) {
            usage();
            System.exit(1);
        }

        MacAddress targetMac = null;
        try {
            targetMac = MacAddress.parseMacAddress(argv[0]);
        } catch (MacAddressFormatException ex) {
            System.out.println("Error: " + ex.getMessage());
        }

        if (targetMac != null) {
            System.out.println("Target MAC address is " + targetMac.toString());
            for (InetAddress addr : getBroadcastAddresses()) {
                System.out.println("Sending WOL packet to broadcast address " + addr.toString().replaceFirst("^[^/]*/", ""));
                for (int j = 0; j < 10; j++) {
                    try {
                        sendWolPacket(addr, targetMac);
                        System.out.print('.');
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            // ignore
                        }
                    } catch (IOException e) {
                        System.out.print('\n');
                        System.out.print("Warning: IOException when sending WOL packet:  " + e.getMessage());
                    }
                }
                System.out.print('\n');
            }
            try {
                InetAddress addr = Inet4Address.getByAddress("", new byte[]{0x0, 0x0, 0x0, 0x0});
                System.out.println("Sending WOL packet to " + addr.toString().replaceFirst("^[^/]*/", ""));
                sendWolPacket(addr, targetMac);
                System.out.println(".");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static Collection<InetAddress> getBroadcastAddresses() {
        Set<InetAddress> broadcastAddresses = new HashSet<>();
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                if (iface.isUp() && !iface.isLoopback() && !iface.isPointToPoint()) {
                    for (InterfaceAddress addr : iface.getInterfaceAddresses()) {
                        System.out.println("Found interface address " + addr.getAddress().toString());
                        if (addr.getBroadcast() != null) {
                            System.out.println("Broadcast address is " + addr.getBroadcast().toString());
                            broadcastAddresses.add(addr.getBroadcast());
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        for (InetAddress addr : broadcastAddresses) {
            System.out.println(addr);
        }
        return broadcastAddresses;
    }

    public static void usage() {
        System.out.println("Usage: wolutil <mac_address>");
    }


    public static void sendWolPacket(InetAddress targetAddr, MacAddress targetMac) throws IOException {
        DatagramSocket mySock = new DatagramSocket();
        byte[] packetBytes = new byte[WOL_PACKET_SIZE];
        byte[] macBytes = targetMac.getAddressBytes();
        System.arraycopy(PAYLOAD_START, 0, packetBytes, 0, PAYLOAD_START.length);
        for (int i = PAYLOAD_START.length; i < WOL_PACKET_SIZE; i+= macBytes.length) {
            System.arraycopy(macBytes, 0, packetBytes, i, macBytes.length);
        }
        SocketAddress addr = new InetSocketAddress(targetAddr, 9);
        mySock.send(new DatagramPacket(packetBytes, packetBytes.length, addr));
    }
}

