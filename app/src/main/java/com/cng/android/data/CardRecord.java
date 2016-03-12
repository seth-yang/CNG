package com.cng.android.data;

import com.cng.android.util.DataUtil;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by game on 2016/3/12
 */
public class CardRecord implements Serializable {
    public byte[] data;

    public boolean admin;
    public int majorVersion, minorVersion;
    public Date expire;
    public int cardNo;

    private CardRecord () {}

    public static CardRecord parse (byte[] data) {
        int header = DataUtil.bytesToInt (data, 2, false);
        int tail   = DataUtil.bytesToInt (data, 14, 2, false);
        if (header != 0xcafe || tail != 0xbabe)
            return null;

        int n = data [2];
        boolean admin = ((n & 0x80) != 0);
        int main_version  = ((n >> 3) & 0x07);
        int min_version   = (n & 0x07);
        long timestamp    = DataUtil.bytesToInt (data, 3, 4, false) * 1000L;
        CardRecord card   = new CardRecord ();
        card.data         = data;
        card.admin        = admin;
        card.majorVersion = main_version;
        card.minorVersion = min_version;
        card.expire       = new Date (timestamp);
        card.cardNo       = DataUtil.bytesToInt (data, 7, 4, false);

        return card;
    }

    public static CardRecord parse (String hex) {
        return parse (DataUtil.fromHex (hex));
    }
}