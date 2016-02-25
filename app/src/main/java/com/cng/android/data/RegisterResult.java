package com.cng.android.data;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

/**
 * Created by game on 2016/2/26
 */
public class RegisterResult extends Result <RegisterResult.Host> {
    public static final class Host implements Serializable {
        @Expose
        private String id, mac;

        public String getId () {
            return id;
        }

        public void setId (String id) {
            this.id = id;
        }

        public String getMac () {
            return mac;
        }

        public void setMac (String mac) {
            this.mac = mac;
        }
    }
}