package com.synopsys.integration.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class StringManagerTest {
    @Test
    public void testStringWidth() throws IOException {
        assertTrue(0 < StringManager.getStringWidth(PDType1Font.HELVETICA, 10.0f, "some text"));
    }

    @ParameterizedTest
    @MethodSource("provideStringWrappingDataStream")
    public void testStringWrapping(StringWrappingData stringWrappingData) throws IOException {
        List<String> actualResults = getActualResults(stringWrappingData);
        assertEquals(stringWrappingData.expectedResults, actualResults);
    }

    @Test
    public void testReallyLongRepeatedString() throws IOException {
        String original = "reallyreallylonglong reallyreallylonglong reallyreallylonglonglong reallyreallylonglong reallyreallylonglong reallyreally longlonglong pants";
        StringWrappingData longRepeated = new StringWrappingData(10.0f, original, 25, Arrays.asList("happy", "monkeyday"));
        List<String> actualResults = getActualResults(longRepeated);
        System.out.println(original);
        System.out.println(StringUtils.join(actualResults, " "));
//        assertEquals(longRepeated.expectedResults, actualResults);
    }

    @Test
    public void testStringRemoving() {
        List<String> allTheWords = new ArrayList<>(Arrays.asList("monkey", "cat", "monkey", "bird", "dog", "bird", "monkey"));
        allTheWords.remove("monkey");
        System.out.println(StringUtils.join(allTheWords, " "));
    }

    private List<String> getActualResults(StringWrappingData stringWrappingData) throws IOException {
        return StringManager.wrapToCombinedList(PDType1Font.HELVETICA, stringWrappingData.fontSize, stringWrappingData.text, stringWrappingData.characterLimit);
    }

    static Stream<StringWrappingData> provideStringWrappingDataStream() {
        return Stream.of(
                new StringWrappingData(1.0f, "happymonkeyday", 5, Arrays.asList("happymonke", "yday"))
                ,new StringWrappingData(10.0f, "happymonkeyday", 5, Arrays.asList("ha", "pp", "ym", "on", "ke", "yd"))
                ,new StringWrappingData(1.0f, "happymonkeyday", 50, Arrays.asList("happymonkeyday"))
                ,new StringWrappingData(10.0f, "happymonkeyday", 50, Arrays.asList("happymonke", "yday"))
                ,new StringWrappingData(1.0f, "happy monkey day", 5, Arrays.asList("happy", "monkey", "day"))
                ,new StringWrappingData(1.0f, "happy monkey day", 50, Arrays.asList("happy monkey day"))
                ,new StringWrappingData(10.0f, "happy monkey day", 50, Arrays.asList("happy", "monkey", "day"))
                ,new StringWrappingData(10.0f, "happymonkey day", 50, Arrays.asList("happymonke", "day"))
                ,new StringWrappingData(10.0f, "happy monkeyday", 50, Arrays.asList("happy", "monkeyday"))
        );
    }

    private static class StringWrappingData {
        public float fontSize;
        public String text;
        int characterLimit;
        List<String> expectedResults;

        public StringWrappingData(float fontSize, String text, int characterLimit, List<String> expectedResults) {
            this.fontSize = fontSize;
            this.text = text;
            this.characterLimit = characterLimit;
            this.expectedResults = expectedResults;
        }
    }

}
