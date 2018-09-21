package me.veppev.avitodriver;

import me.veppev.avitodriver.exception.AnnouncementClosed;
import me.veppev.avitodriver.exception.AnnouncementNotExist;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.*;

import java.io.*;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Класс для взаимодействия с сайтом avito.ru
 * Предоставляет методы для загрузки объявлений
 * TODO вход по аккаунту, отправка сообщений
 */
public class AvitoDriver {

    private Map<String, Announcement> announcementMap = new HashMap<>();
    private volatile HttpHost proxy;
    private final ProxyList proxyList = new ProxyList();

    static final Logger driverLogger = LogManager.getLogger(AvitoDriver.class.getSimpleName());
    private static AvitoDriver avitoDriver = new AvitoDriver();

    public static final String DOMAIN = "https://avito.ru/";
    public static final String MOB_DOMAIN = "https://m.avito.ru/";

    public static AvitoDriver getInstance() {
        return avitoDriver;
    }

    private AvitoDriver() {
        driverLogger.debug("Создан объект драйвера");
    }

    private void changeIP() {
        try {
            proxy = proxyList.getProxyServer();
            driverLogger.warn("Сменён ip на {}", proxy);
        } catch (Exception e) {
            proxy = null;
            driverLogger.error("Не удалось загрузить новое прокси", e);
        }
    }

    private boolean checkBlock(String html) {
        return html.contains("<title>Доступ временно заблокирован</title>")
                || html.contains("<h1>Подождите, идет загрузка.</h1>")
                || html.contains("<title>400 Bad Request</title>")
                || html.contains("alt=\"Доступ временно ограничен\"");
    }

    /**
     * Загружает все объявления по заданной ссылке на авито
     * @param avitoUrl ссылка на поиск авито
     * @return лист с объявлениями
     */
    public List<Announcement> getAnnouncements(AvitoUrl avitoUrl) {
        driverLogger.info("Начало загрузки объявлений по адресу {}", avitoUrl);
        String url = avitoUrl.getUrl();

        String html;
        try {
            html = Network.loadPage(url, proxy);
        } catch (IOException e) {
            driverLogger.error("Не удалось загрузить страницу по адресу {}", avitoUrl);
            changeIP();
            return getAnnouncements(avitoUrl);
            //return Collections.emptyList();
        }
        if (html.contains("<title>Доступ временно заблокирован</title>")
                || html.contains("<h1>Подождите, идет загрузка.</h1>")) {
            driverLogger.error("Авито заблокрировало доступ. Смена IP");
            changeIP();
            return getAnnouncements(avitoUrl);
        }

        if (html.contains("<title>400 Bad Request</title>")) {
            driverLogger.error("Bad request. Смена IP");
            changeIP();
            return getAnnouncements(avitoUrl);
        }

        driverLogger.info("Загружена корректная страница по ссылке {}", avitoUrl);

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

        if (announcements.size() == 0) {
            driverLogger.warn("Не было загружено ни одного объявления по {}\n{}", avitoUrl, html);
        } else {
            driverLogger.info("Загружено {} объявлений по ссылке {}. Новых {} штук",
                    announcements.size(),
                    avitoUrl,
                    countNewAnnouncements);
        }

        return announcements;
    }

    public Announcement loadAnnouncement(int id) throws IOException, AnnouncementClosed, AnnouncementNotExist {
        String page = Network.loadPage(MOB_DOMAIN + id, proxy);
        if (page.contains("Ой! Такой страницы нет :(")) {
            throw new AnnouncementClosed();
        }
        if (page.contains("Авито &mdash; страница не найдена")) {
            throw new AnnouncementNotExist();
        }

        if (checkBlock(page)) {
            driverLogger.warn("При загрузке объявления с id={} авито заблокировало доступ", id);
            changeIP();
            return loadAnnouncement(id);
        }

        String url = Parser.findValue(page, "<meta data-react-helmet=\"true\" property=\"og:url\" content=\"?\"");

        return new Announcement(url, this);
    }

    void loadAnnouncement(Announcement announcement) {
        String url = announcement.getUrl();
        try {
            String html = Network.loadPage(url, proxy);

            if (checkBlock(html)) {
                driverLogger.warn("При загрузке объявления {} авито заблокировало доступ", url);
                changeIP();
                loadAnnouncement(announcement);
                return;
            }

            announcement.setName(Parser.getName(html));
            announcement.setDescription(Parser.getDescription(html));
            announcement.setPrice(Parser.getPrice(html));
            announcement.setImageUrl(Parser.getImageUrls(html));
            announcement.setMetro(Parser.getMetro(html));
            announcement.setOwnerName(Parser.getOwnerName(html));
            announcement.setId(Parser.getId(html));
            driverLogger.info("Объявление по ссылке {} успешно загружено", announcement.getUrl());
        } catch (IOException e) {
            announcement.setDescription("Не удалось загрузить описание");
            announcement.setName("Не удалось загрузить название");
            announcement.setPrice(0);
            announcement.setOwnerName("Не удалось загрузить имя продавца");
            announcement.setMetro("Не удалось загрузить станцию метро");
            driverLogger.error("Не удалось корректно загрузить объявление {}", announcement.getUrl());
        }

    }

    public File downloadImage(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            InputStream is = url.openStream();

            File file = new File(System.nanoTime() + ".jpg");
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
            OutputStream os = new FileOutputStream(file);

            byte[] b = new byte[2048];
            int length;

            while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }

            is.close();
            os.close();

            return file;
        } catch (IOException e) {
            driverLogger.error(e);
            return null;
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
