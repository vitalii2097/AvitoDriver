package me.veppev.avitodriver.chat;

import me.veppev.avitodriver.Announcement;
import me.veppev.avitodriver.exception.AnnouncementNotExist;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Chat {

    private final Announcement announcement;
    private Listener listener;
    private Messenger messenger;
    private int currentId;
    List<String> opponentMessages = new ArrayList<>();

    private static AtomicInteger id = new AtomicInteger(101);

    Chat(Announcement announcement, Listener listener, Messenger messenger) throws AnnouncementNotExist {
        this.announcement = announcement;
        this.listener = listener;
        this.messenger = messenger;
        currentId = id.getAndIncrement();
        messenger.updateChat(this);
    }

    public Announcement getAnnouncement() {
        return announcement;
    }

    void notify(String message) {
        listener.notify(this, message);
    }

    public void send(String message) {
        messenger.send(announcement.getId(), message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chat chat = (Chat) o;
        return Objects.equals(announcement, chat.announcement) &&
                Objects.equals(messenger, chat.messenger);
    }

    @Override
    public int hashCode() {

        return Objects.hash(announcement, messenger);
    }

    public int getId() {
        return currentId;
    }
}
