package ru.p4ejlov0d.galateahunter.model;

import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@SuppressWarnings("ALL")
class LanguageModelTest {
    @Test
    void toTexts() {
        final String[] expected = new String[]{"a", "c", "b", "e", "d", "sfwefewf", "235325"};
        final Component[] actual = LanguageModel.toTexts(expected);

        assertArrayEquals(expected, Arrays.stream(actual).map(Component::getString).toArray(String[]::new));
    }
}