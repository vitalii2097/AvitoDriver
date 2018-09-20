package me.veppev.avitodriver;

import javafx.util.Pair;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;

public class ProxyList {

    static final Logger proxyLogger = LogManager.getLogger(ProxyList.class.getSimpleName());
    private LinkedHashSet<String> usedIPs = new LinkedHashSet<>();

    public HttpHost getProxyServer() {
        String html;

        try {
            html = Network.loadPage("http://www.gatherproxy.com/proxylist/country/?c=Russia", null);
        } catch (IOException e) {
            proxyLogger.error("Не удалось загрузить сайт с прокси", e);
            return null;
        }

        List<String> ipes = Parser.findValues(html, "\"PROXY_IP\":\"?\"");
        List<String> ports = Parser.findValues(html, "\"PROXY_PORT\":\"?\"");
        List<String> times = Parser.findValues(html, "\"PROXY_TIME\":\"?\"");

        Pair<Integer, Integer> proxy = null;

        for (int i = 0; i < ipes.size(); i++) {
            int time = Integer.valueOf(times.get(i));
            if (!usedIPs.contains(ipes.get(i)) && (proxy == null || time < proxy.getValue())) {
                proxy = new Pair<>(i, time);
            }
        }

        if (proxy == null) {
            proxy = new Pair<>(0, 0);
        }

        usedIPs.add(ipes.get(proxy.getKey()));

        return new HttpHost(ipes.get(proxy.getKey()), Integer.valueOf(ports.get(proxy.getKey()), 16));
    }

}
