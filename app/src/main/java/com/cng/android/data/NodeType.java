package com.cng.android.data;

/**
 * Created by game on 2016/2/21
 */
public enum NodeType {
    Temperature ("温度", "T"),
    Humidity ("湿度", "H"),
    Unknown ("未知", "U")
    ;

    private String chinese, shortcut;
    NodeType (String chinese, String shortcut) {
        this.chinese = chinese;
        this.shortcut = shortcut;
    }

    public String getShortcut () {
        return shortcut;
    }

    public String getChinese () {
        return chinese;
    }

    public static NodeType parse (String text) {
        if (text == null || text.trim ().length () == 0)
            return Unknown;

        for (NodeType type : values ()) {
            if (type.shortcut.equals (text) || type.chinese.equals (text)) {
                return type;
            }
        }

        return Unknown;
    }
}