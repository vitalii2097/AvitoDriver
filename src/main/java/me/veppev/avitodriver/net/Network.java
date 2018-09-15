package me.veppev.avitodriver.net;

import org.apache.http.HttpHost;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Вспомогательный класс для работы с сетью
 */
class Network {

    private Request request;

    static {
        System.setProperty("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");
    }

    public Network(String url) {
        request = Request.Get(url);
        request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 YaBrowser/18.2.0.284 Yowser/2.5 Safari/537.36");
    }

    public void setProxy(HttpHost proxy) {
        request.viaProxy(proxy);
    }

    public void setHeader(String name, String value) {
        request.setHeader(name, value);
    }

    public String getPage() throws IOException, IllegalAccessException {
        try {
            Response response = request.execute();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(response.returnResponse().getEntity().getContent()));
            String inputLine;
            StringBuilder responseBuilder = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                responseBuilder.append(inputLine).append('\n');
            }
            return responseBuilder.toString();
        } catch (IOException e) {
            throw e;
        }
    }

    static String loadPage(String url, HttpHost proxy) throws IOException {
        Request request = Request.Get(url);
        request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 YaBrowser/18.2.0.284 Yowser/2.5 Safari/537.36");
        if (proxy != null) {
            request.viaProxy(proxy);
        }

            Response response = request.execute();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(response.returnResponse().getEntity().getContent()));
            String inputLine;
            StringBuilder responseBuilder = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                responseBuilder.append(inputLine).append('\n');
            }
            return responseBuilder.toString();


    }
}
