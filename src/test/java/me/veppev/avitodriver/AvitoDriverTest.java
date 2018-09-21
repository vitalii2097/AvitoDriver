package me.veppev.avitodriver;

import me.veppev.avitodriver.exception.AnnouncementClosed;
import me.veppev.avitodriver.exception.AnnouncementNotExist;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static me.veppev.avitodriver.AvitoDriver.MOB_DOMAIN;
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
        AvitoUrl iphones = createUrl("https://www.avito.ru/rossiya/telefony/iphone?user=1&s_trg=3&q=iphone&s=104");
        assertNotNull(iphones);
        AvitoDriver driver = AvitoDriver.getInstance();
        List<Announcement> announcements = driver.getAnnouncements(iphones);
        assertTrue(announcements.size() > 0);
        System.out.println(announcements.get(0).getName());
    }

    @Test
    public void hardTest() throws IOException {
        AvitoUrl iphones = createUrl("https://www.avito.ru/rossiya/telefony/iphone?user=1&s_trg=3&s=104");
        assertNotNull(iphones);
        AvitoDriver driver = AvitoDriver.getInstance();
        List<Announcement> announcements = driver.getAnnouncements(iphones);
        assertTrue(announcements.size() > 0);
        for (Announcement announcement : announcements) {
            int currentId = announcement.getId();
            System.out.print("Для " + currentId + ": ");
            try {
                Announcement prevAnn = driver.loadAnnouncement(currentId - 1);
                System.out.print("prev " + prevAnn.getName() + " ");
            } catch (AnnouncementNotExist announcementNotExist) {
                System.out.print("prev не существует ");
            } catch (AnnouncementClosed announcementClosed) {
                announcementClosed.printStackTrace();
                System.out.print("prev снято ");
            }

            try {
                Announcement nextAnn = driver.loadAnnouncement(currentId + 1);
                System.out.print("next " + nextAnn.getName() + ";\n");
            } catch (AnnouncementNotExist announcementNotExist) {
                System.out.print("next не существует;\n");
            } catch (AnnouncementClosed announcementClosed) {
                System.out.print("next снято;\n");
            }
        }
    }

    @Test
    public void test() throws IOException {
        AvitoDriver avitoDriver = AvitoDriver.getInstance();
 int i = 1355544;
            try {
                System.out.println(i + ": " + avitoDriver.loadAnnouncement(i).getName());
            } catch (AnnouncementNotExist announcementNotExist) {
                System.out.println(i + ": Не существует");
            } catch (AnnouncementClosed announcementClosed) {
                System.out.println(i + ": Снято");
            }


    }

}