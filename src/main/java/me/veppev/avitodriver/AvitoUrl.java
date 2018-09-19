package me.veppev.avitodriver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AvitoUrl {

    private String url;
    private static final Pattern p = Pattern.compile("(https:\\/\\/www\\.avito\\.ru\\/.+?)(\\?.+)?");
    static final Logger urlLogger = LogManager.getLogger(AvitoUrl.class.getSimpleName());
    private static final AvitoDriver avitoDriver = AvitoDriver.getInstance();

    public AvitoUrl(String url) throws IOException {
        urlLogger.debug("Начата попытка создать AvitoUrl с url={}", url);
        Matcher m = p.matcher(url);
        boolean matches = m.matches();
        if (matches) {
            MatchResult result = m.toMatchResult();
            String baseUrl = result.group(1);
            try {
                String page = avitoDriver.loadAvitoPage(baseUrl);
                if (Parser.checkNotFoundPage(page)) {
                    RuntimeException e = new IllegalArgumentException("Некорректная ссылка. url=" + url);
                    urlLogger.error(e);
                    throw e;
                } else {
                    this.url = url + "&s=104";
                    urlLogger.info("Создан AvitoUrl {}", this);
                }
            } catch (IOException e) {
                urlLogger.error(e);
                throw e;
            }
        } else {
            RuntimeException e = new IllegalArgumentException("Некорректная ссылка. url=" + url);
            urlLogger.error(e);
            throw e;
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
