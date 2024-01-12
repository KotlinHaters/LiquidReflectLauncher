package com.liquidreflect.protocol.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;

public abstract class BaseHandler implements HttpHandler {
    @Override
    public final void handle(HttpExchange exchange) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int status;
        try {
            status = handleRequest(exchange.getRequestBody(), out, exchange);

            if (out.size() == 0) {
                handleStatus(status, out, exchange);
            }
            exchange.sendResponseHeaders(status, out.size());
            if (out.size() != 0) {
                exchange.getResponseBody().write(out.toByteArray());
            }
        } catch (Throwable e) {
            status = 500;
            out = new ByteArrayOutputStream();
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            writer.append("<!DOCTYPE html>\r\n"
                    + "<html>\r\n"
                    + "<head>\r\n"
                    + "	<title>Sorry!</title>\r\n"
                    + "	<meta charset=\"utf-8\">\r\n"
                    + "</head>\r\n"
                    + "<body>\r\n"
                    + "	<h1>Sorry!</h1>\r\n"
                    + "	This request could not be completed due to an unexpected exception.<br/>\r\n"
                    + "	Here is stack trace:\r\n"
                    + "	<pre>");
            e.printStackTrace(new PrintWriter(writer));
            writer.append("</pre>\r\n"
                    + "</body>\r\n"
                    + "</html>");
            writer.flush();
            exchange.sendResponseHeaders(status, out.size());
            exchange.getResponseBody().write(out.toByteArray());
        }
        exchange.close();
    }

    public abstract int handleRequest(InputStream in, OutputStream out, HttpExchange exchange) throws Throwable;

    public void handleStatus(int status, OutputStream out, HttpExchange exchange) throws IOException {
    }
}

