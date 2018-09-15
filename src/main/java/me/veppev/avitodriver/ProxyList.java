package me.veppev.avitodriver;

import javafx.util.Pair;
import org.apache.http.HttpHost;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;

class ProxyList {

    private Network network = new Network("http://www.gatherproxy.com/proxylist/country/?c=Russia");
    private LinkedHashSet<String> usedIPs = new LinkedHashSet<>();

    HttpHost getProxyServer() {
        String html;

        try {
            html = network.getPage();
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
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
