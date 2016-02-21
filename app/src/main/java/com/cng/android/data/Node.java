package com.cng.android.data;

import java.io.Serializable;

/**
 * Created by game on 2016/2/21
 */
public class Node implements Serializable {
    public final long timestamp;
    public final double data;
    public final NodeType type;

    public Node (double data, NodeType type) {
        this.timestamp = System.currentTimeMillis ();
        this.data = data;
        this.type = type;
    }
}