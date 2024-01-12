package com.liquidreflect.protocol.http.handlers;

import com.google.gson.Gson;
import com.liquidreflect.MainApp;
import com.liquidreflect.protocol.http.BaseHandler;
import com.liquidreflect.protocol.http.Utils;
import com.liquidreflect.util.Logger;
import com.sun.net.httpserver.HttpExchange;
import javafx.application.Platform;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

public class Finish extends BaseHandler {
    private final Logger logger = new Logger();

    @Override
    public int handleRequest(InputStream in, OutputStream out, HttpExchange exchange) throws Throwable {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.exit(1);
        }).start();
        return 200;
    }
}