package me.veppev.avitodriver;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class AvitoDriverTest {

    private AvitoUrl createUrl(String url) {
        try {
            return new AvitoUrl(url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Test
    public void getAnnouncements() {
        AvitoUrl iphones = createUrl("https://www.avito.ru/rossiya/telefony/iphone?user=1&s_trg=3&q=iphone+6&s=104");
        assertNotNull(iphones);
        AvitoDriver driver = AvitoDriver.getInstance();
        List<Announcement> announcements = driver.getAnnouncements(iphones);
        assertTrue(announcements.size() > 0);
    }

}