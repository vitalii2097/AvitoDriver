package me.veppev.avitodriver.chat;

import me.veppev.avitodriver.Announcement;
import me.veppev.avitodriver.chat.Chat;
import me.veppev.avitodriver.chat.Listener;
import me.veppev.avitodriver.exception.AnnouncementNotExist;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Messenger {

    private final ChromeDriver driver;
    private final Listener listener;
    private Set<Chat> chats;
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private Map<Chat, ScheduledFuture<?>> futures = new HashMap<>();


    public Messenger(String login, String password, Listener listener) throws IOException {
        this.listener = listener;
        driver = new ChromeDriver();
        driver.get("https://m.avito.ru/profile/settings");

        WebElement log = driver.findElementByName("login");
        log.sendKeys(login);

        WebElement pass = driver.findElementByName("password");
        pass.sendKeys(password);

        WebElement enterButton = driver.findElementsByTagName("input").stream()
                .filter(webElement -> webElement.getAttribute("value").equals("Войти"))
                .findFirst()
                .orElseThrow(IOException::new);

        enterButton.click();

        chats = new HashSet<>();
    }

    public Chat getChat(Announcement announcement) throws AnnouncementNotExist {
        Chat chat = new Chat(announcement, listener, this);
        if (chats.contains(chat)) {
            throw new IllegalArgumentException();
        }
        chats.add(chat);
        ScheduledFuture<?> scheduledFuture = scheduler.scheduleAtFixedRate(() -> {
            try {
                updateChat(chat);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 10, 30, TimeUnit.SECONDS);

        futures.put(chat, scheduledFuture);
        return chat;
    }

    void close(Chat chat) {
        if (chats.contains(chat)) {
            futures.get(chat).cancel(false);
            futures.remove(chat);
            chats.remove(chat);
        }
    }

    void send(int id, String message) {
        synchronized (this) {
            String url = "https://m.avito.ru/profile/messenger/item/" + id;
            driver.get(url);

            boolean finded = false;
            WebElement inputField = null;
            while (!finded) {
                try {
                    if (driver.findElementsByClassName("notify-warning").size() != 0) {
                        return;
                    }
                    if (driver.findElementsByClassName("messenger-notify_alert").size() != 0) {
                        driver.get(url);
                    }
                    inputField = driver.findElementByTagName("textarea");
                    finded = true;
                } catch (NoSuchElementException ignored) {
                }
            }
            inputField.sendKeys(message);

            WebElement sendButton = driver.findElementsByTagName("button").stream()
                    .filter(element -> element.getAttribute("class").contains("channel-footer-send i-messenger-send is-active"))
                    .findFirst().get();
            sendButton.click();
        }
    }

    void updateChat(Chat chat) throws AnnouncementNotExist {
        synchronized (this) {
            String url = "https://m.avito.ru/profile/messenger/item/" + chat.getAnnouncement().getId();
            driver.get(url);

            if (driver.findElementByTagName("title").getText().equals("Авито — страница не найдена     — Объявления на сайте Авито")) {
                throw new AnnouncementNotExist();
            }
            boolean finded = false;
            while (!finded) {
                try {
                    if (driver.findElementsByClassName("notify-warning").size() != 0) {
                        return;
                    }
                    driver.findElementByTagName("textarea");
                    finded = true;
                } catch (NoSuchElementException ignored) {
                }
            }

            List<String> allMessages = driver
                    .findElementsByClassName("channel-message_not_self").stream()
                    .map(element -> element.findElement(By.className("channel-message-content")).getText())
                    .collect(Collectors.toList());

            for (int i = chat.opponentMessages.size(); i < allMessages.size(); i++) {
                chat.notify(allMessages.get(i));
                chat.opponentMessages.add(allMessages.get(i));
            }

        }
    }

}
