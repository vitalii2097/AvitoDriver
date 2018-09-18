package me.veppev.avitodriver;

import org.apache.http.HttpHost;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Класс для взаимодействия с сайтом avito.ru
 * Предоставляет методы для загрузки объявлений
 * TODO вход по аккаунту, отправка сообщений
 */
public class AvitoDriver {

    private Map<String, Announcement> announcementMap = new HashMap<>();
    private volatile HttpHost proxy;
    private final ProxyList proxyList = new ProxyList();

    private static AvitoDriver avitoDriver = new AvitoDriver();

    public static AvitoDriver getInstance() {return avitoDriver;}

    private AvitoDriver() {}

    private void changeIP() {
        proxy = proxyList.getProxyServer();
    }

    private boolean checkBlock(String html) {
        return html.contains("<title>Доступ временно заблокирован</title>")
                || html.contains("<h1>Подождите, идет загрузка.</h1>")
                || html.contains("<title>400 Bad Request</title>");
    }

    /**
     * Загружает все объявления по заданной ссылке на авито
     * @param avitoUrl ссылка на поиск авито
     * @return лист с объявлениями
     */
    public List<Announcement> getAnnouncements(AvitoUrl avitoUrl) {
        String url = avitoUrl.getUrl();

        String html;
        try {
            html = Network.loadPage(url, proxy);
        } catch (IOException e) {
            return Collections.emptyList();
        }
        if (html.contains("<title>Доступ временно заблокирован</title>")
                || html.contains("<h1>Подождите, идет загрузка.</h1>")) {
            System.out.println("Авито заблокрировало доступ, сменяем ip");
            changeIP();
            return getAnnouncements(avitoUrl);
        }

        if (html.contains("<title>400 Bad Request</title>")) {
            System.out.println("Bad request. Смена IP");
            changeIP();
            return getAnnouncements(avitoUrl);
        }

        int countNewAnnouncements = 0;

        List<Announcement> announcements = new ArrayList<>();
        for (String href : Parser.getAnnouncementUrls(html)) {
            if (announcementMap.containsKey(href)) {
                announcements.add(announcementMap.get(href));
            } else {
                Announcement announcement = new Announcement(href, this);
                announcements.add(announcement);
                announcementMap.put(href, announcement);
                countNewAnnouncements++;
            }
        }
        System.out.println("Loaded " + announcements.size() + " announcements by url=" + avitoUrl.getUrl()
                + "; new ann. =" + countNewAnnouncements + "; " + new Date());

        if (announcements.size() == 0) {
            System.out.println(html);
        }

        return announcements;
    }

    void loadAnnouncement(Announcement announcement) {
        String url = announcement.getUrl();
        try {
            String html = Network.loadPage(url, proxy);
            announcement.setName(Parser.getName(html));
            announcement.setDescription(Parser.getDescription(html));
            announcement.setPrice(Parser.getPrice(html));
            announcement.setImageUrl(Parser.getImageUrls(html));
            announcement.setMetro(Parser.getMetro(html));
            announcement.setOwnerName(Parser.getOwnerName(html));
        } catch (IOException | IllegalArgumentException e) {
            announcement.setDescription("Не удалось загрузить описание");
            announcement.setName("Не удалось загрузить название");
            announcement.setPrice(0);
            announcement.setOwnerName("Не удалось загрузить имя продавца");
            announcement.setMetro("Не удалось загрузить станцию метро");
        }

    }

    String loadAvitoPage(String url) throws IOException {
        try {
            return Network.loadPage(url, proxy);
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Нет подключения к интернету");
        }
    }

}
