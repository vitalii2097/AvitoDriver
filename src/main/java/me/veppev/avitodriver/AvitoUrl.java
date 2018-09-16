package me.veppev.avitodriver;

import java.io.IOException;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AvitoUrl {

    private String url;
    private static final Pattern p = Pattern.compile("(https:\\/\\/www\\.avito\\.ru\\/.+?)(\\?.+)?");
    private static final AvitoDriver avitoDriver = AvitoDriver.getInstance();

    public AvitoUrl(String url) throws IOException {
        Matcher m = p.matcher(url);
        boolean matches = m.matches();
        if (matches) {
            MatchResult result = m.toMatchResult();
            String baseUrl = result.group(1);
            try {
                String page = avitoDriver.loadAvitoPage(baseUrl);
                if (Parser.checkNotFoundPage(page)) {
                    throw new IllegalArgumentException("Некорректная ссылка. url=" + url);
                } else {
                    this.url = url + "&s=104";
                }
            } catch (IOException e) {
                throw e;
            }
        } else {
            throw new IllegalArgumentException("Некорректная ссылка. url=" + url);
        }
    }

    String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "url='" + url + ';';
    }
}
