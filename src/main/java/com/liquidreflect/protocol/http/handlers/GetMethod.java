package com.liquidreflect.protocol.http.handlers;

import com.google.gson.Gson;
import com.liquidreflect.MainApp;
import com.liquidreflect.protocol.http.BaseHandler;
import com.liquidreflect.protocol.http.Utils;
import com.liquidreflect.util.Logger;
import com.sun.net.httpserver.HttpExchange;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

public class GetMethod extends BaseHandler {
    private final Logger logger = new Logger();

    @Override
    public int handleRequest(InputStream in, OutputStream out, HttpExchange exchange) throws Throwable {
        HashMap<String, String> parms = Utils.query(exchange.getRequestURI().getQuery());
        HashMap<String, Object> resp = new HashMap<>();
		if (parms.containsKey("name")) {
            String name = MainApp.mapping.getMethodName(new String(Base64.getDecoder().decode(parms.get("name"))));
            logger.info("Get Mapping: " + name);
            resp.put("result", name);
        }
        out.write(new Gson().toJson(resp).getBytes(StandardCharsets.UTF_8));
        return 200;
    }
}