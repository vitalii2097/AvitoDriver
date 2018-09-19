package me.veppev.avitodriver;

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
                || html.contains("<title>400 Bad Request</title>");
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
            return Collections.emptyList();
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

            List<String> imageUrls = Parser.getImageUrls(html);
            List<File> images = imageUrls.stream().map(this::downloadImage).collect(Collectors.toList());

            announcement.setImageUrl(images);
            announcement.setMetro(Parser.getMetro(html));
            announcement.setOwnerName(Parser.getOwnerName(html));
            driverLogger.info("Объявление по ссылке {} успешно загружено", announcement.getUrl());
        } catch (IOException | IllegalArgumentException e) {
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
