package me.veppev.avitodriver;

import avito.net.Announcement;
import avito.net.AvitoDriver;
import core.IListener;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

class Checker implements Runnable {

    private Query query;
    private AvitoDriver driver;

    private void updateQuery() {
        System.out.println("start load " + new Date());
        List<Announcement> announcements = driver.getAnnouncements(query.getUrl());
        announcements.forEach(query::addAnnouncement);
        System.out.println("Updated query: " + query);
    }

    Checker(Query query, AvitoDriver driver) {
        this.query = query;
        this.driver = driver;

        updateQuery();
    }

    @Override
    public void run() {
        updateQuery();
    }
}


public class AvitoChecker {

    private Map<Url, Query> queries;
    private AvitoDriver driver = new AvitoDriver();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    public AvitoChecker() {
        queries = new HashMap<>();
    }

    public void addListener(IListener listener, Url url) {
        if (!queries.containsKey(url)) {
            Query query = new Query(url);
            queries.put(url, query);

            int seconds = new Date().getSeconds();
            scheduler.scheduleAtFixedRate(new Checker(query, driver), 65 - seconds, 60, TimeUnit.SECONDS);
            System.out.println("Added new query: " + query);
        }

        Query query = queries.get(url);
        query.addListener(listener);
        System.out.println("Added new listener=" + listener + " to query=" + query);
    }

    public void clearListener(IListener listener) {
        queries.values().forEach(query -> query.dropListener(listener));
    }

    public List<Url> getUrls(IListener listener) {
        return queries.values()
                .stream()
                .filter(query -> query.contains(listener))
                .map(Query::getUrl)
                .collect(Collectors.toList());
    }

}
