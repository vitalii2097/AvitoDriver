package me.veppev.avitodriver;

import me.veppev.avitodriver.chat.Chat;
import me.veppev.avitodriver.chat.Listener;
import me.veppev.avitodriver.chat.Messenger;
import org.junit.Test;

public class MessengerTest {

    @Test
    public void updateChat() throws Exception {

        Listener listener = (chat, message) -> System.out.println(chat.getId() + ": " + message);
        Messenger messenger = AvitoDriver.getInstance()
                .getMessenger("vepppev4@rambler.ru", "Veppev1997", listener);

        Chat chat = messenger.getChat(AvitoDriver.getInstance().loadAnnouncement(960211429));

        chat.send("где можно встретиться");

        Thread.sleep(60000);

    }
}