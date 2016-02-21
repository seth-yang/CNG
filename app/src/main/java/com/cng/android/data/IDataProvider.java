package com.cng.android.data;

import java.util.Queue;

/**
 * Created by game on 2016/2/22
 */
public interface IDataProvider {
    Queue<Transformer> getNodes ();
}
