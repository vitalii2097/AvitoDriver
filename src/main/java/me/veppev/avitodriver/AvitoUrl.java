package me.veppev.avitodriver;

public class AvitoUrl implements Url {

    private String url;

    public AvitoUrl(String url) {
        this.url = url;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "AvitoUrl{" +
                "url='" + url + '\'' +
                '}';
    }
}
