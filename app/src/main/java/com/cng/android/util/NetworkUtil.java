package com.cng.android.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by game on 2016/2/23
 */
public class NetworkUtil {
    public static List<InetAddress> getNetworkInterface () throws SocketException {
        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces ();
        List<InetAddress> ret = new ArrayList<> ();
        while (e.hasMoreElements ()) {
            NetworkInterface ni = e.nextElement ();
            if (!ni.isUp ()) continue;
            if (ni.isLoopback ()) continue;
            byte[] mac = ni.getHardwareAddress ();
            if (mac == null) continue;
            if (ni.isPointToPoint ()) continue;
            List<InterfaceAddress> list = ni.getInterfaceAddresses ();
            if (list == null || list.isEmpty ()) continue;

            for (InterfaceAddress ia : list) {
                InetAddress address = ia.getAddress ();
                if (address instanceof Inet4Address) {
                    ret.add (address);
                }
            }
        }
        return ret;
    }
}