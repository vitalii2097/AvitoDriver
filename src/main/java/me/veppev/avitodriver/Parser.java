package me.veppev.avitodriver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Parser {

    static List<String> findValues(String code, String pattern) {
        String leftPatternPart = pattern.substring(0, pattern.indexOf('?'));
        String rightPatternPart = pattern.substring(pattern.indexOf('?') + 1);

        List<String> resultingList = new ArrayList<>();
        int startIndex = 0;
        int beginIndex;
        int endIndex;

        while ((beginIndex = code.indexOf(leftPatternPart, startIndex)) != -1) {
            endIndex = code.indexOf(rightPatternPart, beginIndex + leftPatternPart.length());
            if (endIndex == -1) {
                break;
            }
            startIndex = endIndex;
            resultingList.add(code.substring(beginIndex + leftPatternPart.length(), endIndex));
        }
        return resultingList;
    }

    static String findValue(String code, String pattern) {
        List<String> values = findValues(code, pattern);
        if (values.isEmpty()) {
            throw new IllegalArgumentException(code);
        } else {
            return values.get(0);
        }

    }

    static List<String> getAnnouncementUrls(String code) {
        String domain = "https://www.avito.ru";
        List<String> values = findValues(code, "<a class=\"item-description-title-link\"\n" +
                " itemprop=\"url\"\n" +
                " ?>");
        List<String> announcementUrls = new ArrayList<>();
        for (String value : values) {
            String href = domain + findValue(value, "href=\"?\"");
            announcementUrls.add(href);
        }
        return announcementUrls;
    }

    static String getName(String code) {
        String pattern = "<span class=\"title-info-title-text\" itemprop=\"name\">?</span>";
        return findValue(code, pattern);
    }

    static String getMetro(String code) {
        String pattern = "<div class=\"seller-info-label\">Адрес</div> <div class=\"seller-info-value\">\n" +
                " ?\n" +
                " </div>";
        try {
            return findValue(code, pattern);
        } catch (IllegalArgumentException e) {
            return "Без адреса";
        }
    }

    static String getOwnerName(String code) {
        String pattern = "title=\"Нажмите, чтобы перейти в профиль\">\n" +
                " ?\n" +
                " </a>";
        try {
            return findValue(code, pattern);
        } catch (IllegalArgumentException e) {
            return "Без имени";
        }
    }

    static String getDescription(String code) {
        String pattern = "<div class=\"item-description-text\" itemprop=\"description\">\n" +
                "  <p>?</p>  </div>";
        try {
            return findValue(code, pattern)
                    .replace("<br>", "\n")
                    .replace("<br />", "\n")
                    .replace("</p>", "\n")
                    .replace("<p>", "");
        } catch (IllegalArgumentException e) {
            pattern = "<div class=\"item-description-html\" itemprop=\"description\">?</div>";
            return findValue(code, pattern)
                    .replace("<br>", "\n")
                    .replace("<br />", "\n")
                    .replace("</p>", "\n")
                    .replace("<p>", "");
        }
    }

    static int getPrice(String code) {
        String pattern = "<span class=\"js-item-price\" content=\"?\" itemprop=\"price\">";
        try {
            String price = findValue(code, pattern);
            return Integer.parseInt(price);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    static List<String> getImageUrls(String code) {
        String pattern = "<div class=\"gallery-img-frame js-gallery-img-frame\"\n" +
                " data-url=\"//?\"";
        try {
            List<String> urls = findValues(code, pattern);
            for (int i = 0; i < urls.size(); i++) {
                urls.set(i, "http://" + urls.get(i));
            }
            return urls;
        } catch (IllegalArgumentException e) {
            return Collections.emptyList();
        }
    }

}
