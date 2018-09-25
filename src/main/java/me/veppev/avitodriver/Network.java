package me.veppev.avitodriver;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.nio.ch.Net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Вспомогательный класс для работы с сетью
 */
class Network {

    private Request request;
    static final Logger networkLogger = LogManager.getLogger(Network.class.getSimpleName());

    static {
        System.setProperty("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");
    }

    public Network(String url) {
        request = Request.Get(url);
        request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 YaBrowser/18.2.0.284 Yowser/2.5 Safari/537.36");
    }

    static String loadPage(String url, HttpHost proxy) throws IOException {
        networkLogger.info("Начата загрузка {} с использованием proxy={}", url, proxy);
        Request request = Request.Get(url);
        request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 YaBrowser/18.2.0.284 Yowser/2.5 Safari/537.36");
        if (proxy != null) {
            request.viaProxy(proxy);
        }

        //TODO тут вылетает HttpHostConnectException
        Response response = request.execute();
        HttpResponse httpResponse = response.returnResponse();
        if (httpResponse.getStatusLine().getStatusCode() == 204) {
            return "";
        }
        BufferedReader in = new BufferedReader(
                new InputStreamReader(httpResponse.getEntity().getContent()));
        String inputLine;
        StringBuilder responseBuilder = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            responseBuilder.append(inputLine).append('\n');
        }
        networkLogger.info("Страница {} успешно загружена", url);
        return responseBuilder.toString();
    }
}
