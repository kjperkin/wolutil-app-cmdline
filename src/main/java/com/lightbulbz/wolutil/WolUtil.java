package com.lightbulbz.wolutil;

import com.lightbulbz.net.MacAddress;
import com.lightbulbz.net.MacAddressFormatException;
import com.lightbulbz.net.NetUtils;
import com.lightbulbz.net.WOLPacketSender;

import java.io.IOException;
import java.net.*;

public class WolUtil {

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
            for (InetAddress addr : NetUtils.getBroadcastAddresses()) {
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

    public static void usage() {
        System.out.println("Usage: wolutil <mac_address>");
    }

    public static void sendWolPacket(InetAddress targetAddr, MacAddress targetMac) throws IOException {
        new WOLPacketSender(targetAddr, targetMac).sendPacket();
    }

}

