package me.veppev.avitodriver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.*;

/**
 * Представляет объявление с авито. Загружает информацию по "требованию"
 */
public class Announcement {

    private String name;
    private String description;
    private int price;
    private String url;
    private List<String> imageUrls;
    private String metro;
    private String ownerName;
    static final Logger annLoger = LogManager.getLogger(Announcement.class.getSimpleName());

    private boolean loaded = false;
    private AvitoDriver driver;

    private synchronized void load() {
        if (!loaded && driver != null) {
            annLoger.info("Загрузка объявления "+ this.url);
            driver.loadAnnouncement(this);

            loaded = true;
        }
    }

    Announcement(String url, AvitoDriver avitoDriver) {
        if (url.contains("?")) {
            this.url = url.substring(0, url.indexOf("?"));
        } else {
            this.url = url;
        }
        driver = avitoDriver;
        imageUrls = new ArrayList<>();
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        load();
        return name;
    }

    public String getDescription() {
        load();
        return description;
    }

    public int getPrice() {
        load();
        return price;
    }

    public List<File> getImageUrls() {
        return imageUrls.stream().map(driver::downloadImage).collect(Collectors.toList());
    }

    public String getMetro() {
        return metro;
    }

    public String getOwnerName() {
        return ownerName;
    }

    void setName(String name) {
        this.name = name;
    }

    void setDescription(String description) {
        this.description = description;
    }

    void setPrice(int price) {
        this.price = price;
    }

    void setImageUrl(List<String> urls) {
        imageUrls = urls;
    }

    void setMetro(String metro) {
        this.metro = metro;
    }

    void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    @Override
    public String toString() {
        load();
        return "Announcement{" +
                "name='" + name + '\'' +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Announcement that = (Announcement) o;
        return Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {

        return Objects.hash(url);
    }
}
