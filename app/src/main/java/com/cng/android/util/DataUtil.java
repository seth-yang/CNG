package com.cng.android.util;

import com.cng.android.data.EnvData;
import com.cng.android.data.Event;
import com.cng.android.data.ExchangeData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by game on 2016/2/28
 */
public class DataUtil {
    private static final char[] LETTER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * 对数组切片
     * @param buff   原始数组
     * @param start  开始位置
     * @param length 切片长度
     * @return 切片后的新数组
     */
    public static byte[] slice (byte[] buff, int start, int length) {
        if (buff == null || buff.length == 0)
            return new byte[] {};

        if (start < 0 || start + length > buff.length)
            throw new ArrayIndexOutOfBoundsException ("start + length must less than array's length");

        byte[] tmp = new byte[length];
        System.arraycopy (buff, start, tmp, 0, length);
        return tmp;
    }

    /**
     * 反转数组.
     *
     * 在源数组上将数组元素反转。若要返回一个新数组，应使用 {@link #reverseTo(byte[])}
     * @param data 源数组
     */
    public static void reverse (byte[] data) {
        reverse (data, 0, data.length);
    }

    /**
     * 对数组的部分区域进行反转.
     *
     * 在源数组上针对部分区间进行元素的反转。若要返回一个反转后的数据区间切片，应使用 {@link #reverseTo(byte[], int, int)}
     * @param data 源数组
     * @param start 区间开始位置。若无效的区间开始位置（start &lt; 0 || start + length &gt; data.length)，则抛出数组越界异常
     * @param length 区间长度
     */
    public static void reverse (byte[] data, int start, int length) {
        int half = length / 2;
        byte tmp;
        for (int i = 0; i < half; i ++) {
            tmp = data [i + start];
            data [i + start] = data [start + length - i - 1];
            data [start + length - i - 1] = tmp;
        }
    }

    /**
     * 反转数组并返回一个新的数组.
     *
     * 反转不会影响源数组。
     * 参见 {@link #reverse(byte[])}, {@link #reverse(byte[], int, int)}, {@link #reverseTo(byte[], int, int)}
     * @param data 源数组
     * @return 反转后的数组.
     */
    public static byte[] reverseTo (byte[] data) {
        return reverseTo (data, 0, data.length);
    }

    /**
     * 反转源数组的指定区域，并返回反转部分数组的新的拷贝.
     *
     * 注意，该方法不会影响源数组。
     * 参见 {@link #reverse(byte[])}, {@link #reverse(byte[], int, int)}, {@link #reverseTo(byte[])}
     * @param data 源数组
     * @param start 区间开始
     * @param length 长度
     * @return 反转后，新的区间数组
     */
    public static byte[] reverseTo (byte[] data, int start, int length) {
        byte[] buff = slice (data, start, length);
        reverse (buff);
        return buff;
    }

    /**
     * 将字节数组拼装成整数.
     *
     * @param data 字节数组
     * @return 拼装后的整数
     */
    public static int bytesToInt (byte[] data) {
        if (data == null || data.length == 0)
            throw new NumberFormatException ("can't cast empty to int");

        if (data.length > 4)
            throw new RuntimeException ("integer value overflow.");

        int n = 0;
        for (int i = 0; i < data.length; i ++) {
            n |= (data [i] & 0xff) << ((data.length - i - 1) * 8);
        }

        return n;
    }

    /**
     * 将字节数组的指定部分拼装成整数.
     *
     * 若参数 reverse 为真，则先将指定的区域进行反转，然后再进行拼装.
     *
     * @param data 字节数组
     * @param start 开始位置
     * @param length 长度
     * @param reverse 是否反转字节数组
     * @return 拼装后的整数值
     */
    public static int bytesToInt (byte[] data, int start, int length, boolean reverse) {
        byte[] tmp = slice (data, start, length);
        if (reverse)
            reverse (tmp);

        return bytesToInt (tmp);
    }

    /**
     * 将字节数组的指定部分拼装成整数.
     *
     * 若参数 reverse 为真，则先将指定的区域进行反转，然后再进行拼装.
     *
     * @param data 字节数组
     * @param length 长度
     * @param reverse 是否反转字节数组
     * @return 拼装后的整数值
     */
    public static int bytesToInt (byte[] data, int length, boolean reverse) {
        return bytesToInt (data, 0, length, reverse);
    }

    /**
     * 将字节数组的指定部分拼装成整数.
     *
     * 若参数 reverse 为真，则先将指定的区域进行反转，然后再进行拼装.
     *
     * @param reverse 是否反转字节数组
     * @return 拼装后的整数值
     */
    public static int bytesToInt (byte[] buff, boolean reverse) {
        return bytesToInt (buff, 0, buff.length, reverse);
    }

    /**
     * 将整数拆分位字节数组.
     *
     * 返回的数组长度为 4.若要指定返回的数组的长度，请使用 {@link #intToBytes(int, int)},
     * 若需要将返回的字节数组反转，则使用 {@link #intToBytes(int, int, boolean)}
     *
     * @param n 整数值
     * @return 拆分后的字节数组
     */
    public static byte[] intToBytes (int n) {
        return intToBytes (n, 4);
    }

    /**
     * 将整数拆分位字节数组.
     *
     * 若需要将返回的字节数组反转，则使用 {@link #intToBytes(int, int, boolean)}
     *
     * @param n 整数值
     * @param length 指定的返回的字节数组的长度，其值不能超过4。
     * @return 拆分后的字节数组
     */
    public static byte[] intToBytes (int n, int length) {
        if (length > 4)
            length = 4;
        byte[] buff = new byte[length];
        for (int i = 0; i < buff.length; i ++) {
            buff [i] = (byte) ((n >> ((length - i - 1) * 8)) & 0xff);
        }
        return buff;
    }

    /**
     * 将整数拆分位字节数组.
     *
     * 若参数 reverse 为 真，则对返回的字节数组进行反转
     *
     * @param n 整数值
     * @param length 指定的返回的字节数组的长度，其值不能超过4。
     * @return 拆分后的字节数组
     */
    public static byte[] intToBytes (int n, int length, boolean reverse) {
        byte[] buff = intToBytes (n, length);
        if (reverse)
            reverse (buff);
        return buff;
    }

    /**
     * 将字节数组的指定部分拼装成长整数.
     *
     * @param data 字节数组
     * @return 拼装后的整数值
     */
    public static long bytesToLong (byte[] data) {
        if (data == null || data.length == 0)
            throw new NumberFormatException ("can't cast empty to long");

        if (data.length > 8)
            throw new RuntimeException ("long value overflow.");

        long n = 0;
        for (int i = 0; i < data.length; i ++) {
            n |= (data [i] & 0xffL) << ((data.length - i - 1) * 8);
        }
        return n;
    }

    /**
     * 将字节数组的指定部分拼装成长整数.
     *
     * 若参数 reverse 为真，则先将指定的区域进行反转，然后再进行拼装.
     *
     * @param data 字节数组
     * @param reverse 是否反转字节数组
     * @return 拼装后的整数值
     */
    public static long bytesToLong (byte[] data, boolean reverse) {
        return bytesToLong (data, 0, 8, reverse);
    }

    /**
     * 将字节数组的指定部分拼装成长整数.
     *
     * 若参数 reverse 为真，则先将指定的区域进行反转，然后再进行拼装.
     *
     * @param data 字节数组
     * @param length 长度
     * @param reverse 是否反转字节数组
     * @return 拼装后的整数值
     */
    public static long bytesToLong (byte[] data, int length, boolean reverse) {
        return bytesToLong (data, 0, length, reverse);
    }

    /**
     * 将字节数组的指定部分拼装成整数.
     *
     * 若参数 reverse 为真，则先将指定的区域进行反转，然后再进行拼装.
     *
     * @param data 字节数组
     * @param start 起始位置
     * @param length 长度
     * @param reverse 是否反转字节数组
     * @return 拼装后的整数值
     */
    public static long bytesToLong (byte[] data, int start, int length, boolean reverse) {
        byte[] buff = slice (data, start, length);
        if (reverse)
            reverse (buff);

        return bytesToLong (buff);
    }

    /**
     * 将长整数拆分位字节数组.
     *
     * 若需要指定返回数组的长度，请使用 {@link #longToBytes(long, int)}
     * 若需要将返回的字节数组反转，则使用 {@link #intToBytes(int, int, boolean)}
     *
     * @param n 整数值
     * @return 拆分后的字节数组
     */
    public static byte[] longToBytes (long n) {
        return longToBytes (n, 8);
    }

    /**
     * 将长整数拆分位字节数组.
     *
     * 若需要将返回的字节数组反转，则使用 {@link #intToBytes(int, int, boolean)}
     *
     * @param n 整数值
     * @param length 指定返回数组的长度
     * @return 拆分后的字节数组
     */
    public static byte[] longToBytes (long n, int length) {
        if (length < 0 || length > 8)
            throw new RuntimeException ("Long value overflow.");

        byte[] buff = new byte[length];
        for (int i = 0; i < length; i ++) {
            buff [i] = (byte) ((n >> ((length - i - 1) * 8)) & 0xff);
        }

        return buff;
    }

    /**
     * 将长整数拆分位字节数组.
     *
     * 若参数 reverse 为真，则先将指定的区域进行反转，然后再进行拼装.
     *
     * @param n 整数值
     * @param length 指定返回数组的长度
     * @param reverse 是否反转
     * @return 拆分后的字节数组
     */
    public static byte[] longToBytes (long n, int length, boolean reverse) {
        byte[] tmp = longToBytes (n, length);
        if (reverse)
            reverse (tmp);

        return tmp;
    }

    public static String toHex (byte[] buff) {
        StringBuilder builder = new StringBuilder (buff.length * 3);
        for (byte b : buff) {
            if (builder.length () > 0) builder.append (' ');
            builder.append (LETTER [(b >> 4) & 0xf])
                    .append (LETTER [b & 0xf]);
        }
        return builder.toString ();
    }

    public static byte[] fromHex (String hex) {
        if (hex == null || hex.trim ().length () == 0)
            return new byte[0];

        String[] a = hex.trim ().split ("[\\s,\\-]");
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        for (String p : a) {
            int x, y;
            char c = p.charAt (0);
            if (c >= '0' && c <= '9') {
                x = c - '0';
            } else if (c >= 'a' && c <= 'f') {
                x = 10 + c - 'a';
            } else if (c >= 'A' && c <= 'F') {
                x = 10 + c - 'A';
            } else {
                throw new NumberFormatException (hex);
            }

            c = p.charAt (1);
            if (c >= '0' && c <= '9') {
                y = c - '0';
            } else if (c >= 'a' && c <= 'f') {
                y = 10 + c - 'a';
            } else if (c >= 'A' && c <= 'F') {
                y = 10 + c - 'A';
            } else {
                throw new NumberFormatException (hex);
            }

            baos.write ((x << 4) | y);
        }

        return baos.toByteArray ();
    }

    /**
     * 将整数形式的IP地址值转成字符串形式.
     *
     * 逆操作参见 {@link #stringToIp(String)}
     *
     * @param ip ip地址的整数形式
     * @return ip地址的字符串形式
     */
    public static String ipToString (int ip) {
        byte[] buff = intToBytes (ip);
        StringBuilder builder = new StringBuilder ();
        for (byte b : buff) {
            if (builder.length () > 0) builder.append ('.');
            builder.append (b & 0xff);
        }
        return builder.toString ();
    }

    /**
     * 将字符串形式的IP地址表达式转成整数形式.
     * 逆操作参见 {@link #ipToString(int)}
     * @param ip 字符串形式的IP地址表达式
     * @return IP地址的整数值
     */
    public static int stringToIp (String ip) {
        String[] tmp = ip.split ("\\.");
        byte[] buff = new byte[4];
        for (int i = 0; i < 4; i ++) {
            buff [i] = (byte) (Integer.parseInt (tmp [i]) & 0xff);
        }
        return bytesToInt (buff);
    }

    public static byte[] memset (byte value, int length) {
        byte[] buff = new byte[length];
        memset (buff, value);
        return buff;
    }

    public static void memset (byte[] buff, byte value) {
        for (int i = 0; i < buff.length; i ++) {
            buff [i] = value;
        }
    }

    public static void memset (byte[] buff, byte value, int start, int length) {
        for (int i = start; i < start + length; i ++) {
            buff [i] = value;
        }
    }

    public static String toString (int[] array) {
        return Arrays.toString (array);
    }

    private static void println (byte[] buff) throws IOException {
        println (buff, System.out);
    }

    private static void println (byte[] buff, OutputStream out) throws IOException {
        out.write (toHex (buff).getBytes ());
        out.write ('\r');
        out.write ('\n');
        out.flush ();
    }

    public static Map<String, Object> toMap (List<ExchangeData> data) {
        Map<String, Object> map = new HashMap<> ();
        for (ExchangeData e : data) {
            if (e.data != null) {
                @SuppressWarnings ("unchecked")
                List<EnvData> list = (List<EnvData>) map.get ("D");
                if (list == null) {
                    list = new ArrayList<> ();
                    map.put ("D", list);
                }
                list.add (e.data);
            }
            if (e.event != null) {
                @SuppressWarnings ("unchecked")
                List<Event> list = (List<Event>) map.get ("E");
                if (list == null) {
                    list = new ArrayList<> ();
                    map.put ("E", list);
                }
                list.add (e.event);
            }
        }

        return map;
    }
}
