package com.cng.android.util;

import com.cng.android.data.EnvData;
import com.cng.android.data.Event;
import com.cng.android.data.ExchangeData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by game on 2016/2/28
 */
public class DataUtil {
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
