package me.veppev.avitodriver;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class AvitoUrlTest {

    @Test
    public void createInstance() {
        try {
            AvitoUrl avitoUrl = new AvitoUrl("https://www.avito.ru/sankt-peterburg_pushkin/ptitsy?s_trg=3&q=продам");
            assertEquals("https://www.avito.ru/sankt-peterburg_pushkin/ptitsy?s_trg=3&q=продам", avitoUrl.getUrl());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            fail();
        }

        try {
            AvitoUrl avitoUrl = new AvitoUrl("https://www.avito.ru/sankt-peterburg_pushkin/ptitsy?s_trg=3&q=продам&?dkjgndkjgnjdf");
            assertEquals("https://www.avito.ru/sankt-peterburg_pushkin/ptitsy?s_trg=3&q=продам&?dkjgndkjgnjdf", avitoUrl.getUrl());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            fail();
        }

        try {
            AvitoUrl avitoUrl = new AvitoUrl("https://www.avito.ru/sankt-peterburg_pushkin/ptitsy");
            assertEquals(avitoUrl.getUrl(), "https://www.avito.ru/sankt-peterburg_pushkin/ptitsy");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            fail();
        }

        try {
            AvitoUrl avitoUrl = new AvitoUrl("https://www.avito.ru/sankt-petfdsdrg_pushkin/ptitsy?s_trg=3&q=продам");
            fail();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            AvitoUrl avitoUrl = new AvitoUrl("huita.avito.ru/sankt-peterburg_pushkin/ptitsy?s_trg=3&q=продам");
            fail();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException ignored) {
        }
    }

}