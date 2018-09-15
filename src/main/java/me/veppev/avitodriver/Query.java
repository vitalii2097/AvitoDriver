package me.veppev.avitodriver;

import avito.net.Announcement;
import core.IListener;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

class Query implements Iterable<Announcement> {

    private final Url url;
    private List<Announcement> announcements;
    private Set<IListener> listeners;
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(20);

    Query(Url url) {
        this.url = url;
        announcements = new ArrayList<>();
        listeners = new HashSet<>();
    }

    @Override
    public String toString() {
        return "Query{" +
                "url=" + url +
                '}';
    }

    void addAnnouncement(Announcement announcement) {
        if (!announcements.contains(announcement)) {
            announcements.add(announcement);
            for (IListener listener : listeners) {
                scheduler.execute(listener::notifyNewAnnouncement);
            }
        }
    }

    void addListener(IListener listener) {
        if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
        listener.setIterator(iterator());
    }

    void dropListener(IListener listener) {
        listeners.remove(listener);
    }

    public Url getUrl() {
        return url;
    }

    boolean contains(IListener listener) {
        return listeners.contains(listener);
    }

    @Override
    public Iterator<Announcement> iterator() {
        return new QueryIterator();
    }

    private class QueryIterator implements Iterator<Announcement> {

        private int index = 0;

        @Override
        public boolean hasNext() {
            return announcements.size() > index;
        }

        @Override
        public Announcement next() {
            return announcements.get(index++);
        }
    }
}
