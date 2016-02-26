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
    public static List<NetworkInterface> getNetworkInterfaces () throws SocketException {
        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces ();
        List<NetworkInterface> ret = new ArrayList<> ();
        while (e.hasMoreElements ()) {
            NetworkInterface ni = e.nextElement ();
            if (!ni.isUp () || ni.isLoopback () || ni.isPointToPoint ())
                continue;

            byte[] mac = ni.getHardwareAddress ();
            if (mac == null)
                continue;

            List<InterfaceAddress> list = ni.getInterfaceAddresses ();
            if (list == null || list.isEmpty ())
                continue;

            ret.add (ni);
        }
        return ret;
    }

    public static List<InetAddress> getInetAddresses () throws SocketException {
        List<InetAddress> ret = new ArrayList<> ();
        List<NetworkInterface> list = getNetworkInterfaces ();
        for (NetworkInterface ni : list) {
            for (InterfaceAddress ia : ni.getInterfaceAddresses ()) {
                InetAddress address = ia.getAddress ();
                if (address instanceof Inet4Address)
                    ret.add (address);
            }
        }
        return ret;
    }

    public static List<byte[]> getMacAddresses () throws SocketException {
        List<NetworkInterface> list = getNetworkInterfaces ();
        List<byte[]> ret = new ArrayList<> (list.size ());
        for (NetworkInterface ni : list) {
            ret.add (ni.getHardwareAddress ());
        }
        return ret;
    }
}