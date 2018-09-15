package me.veppev.avitodriver;

public class AvitoUrl {

    private String url;

    public AvitoUrl(String url) {
        this.url = url;
    }

    String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "AvitoUrl{" +
                "url='" + url + '\'' +
                '}';
    }
}
