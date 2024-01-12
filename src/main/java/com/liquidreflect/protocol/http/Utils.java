package com.liquidreflect.protocol.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

public class Utils {

    private Utils() {

    }

    public static HashMap<String, String> query(String str) {
        //System.out.println(str);
        HashMap<String, String> map = new HashMap<>();
        if (str == null)
            return map;
        String[] parms = str.split("&");
        for (String s : parms) {
            int pos = s.indexOf('=');
            if (pos != -1) {
                String key = s.substring(0, pos);
                String val = s.substring(pos + 1);
                try {
                    map.put(URLDecoder.decode(key, "UTF-8"), URLDecoder.decode(val, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else
                try {
                    map.put(URLDecoder.decode(s, "UTF-8"), ""); // empty key.
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
        return map;
    }
}
