/**
 * integration-reporting
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.pdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding;

public class StringManager {
    public static List<String> wrapToCombinedList(final String str, final int charLimit) throws IOException {
        return wrapToCombinedList(PDFBoxManager.DEFAULT_FONT, PDFBoxManager.DEFAULT_FONT_SIZE, str, charLimit);
    }

    public static List<String> wrapToCombinedList(final PDFont font, final float fontSize, final String str, final int charLimit) throws IOException {
        final ArrayList<String> subWords = new ArrayList<>(Arrays.asList(str.split(" ")));
        String previousBrokenWord = "";
        // break up string if it is too long
        for (int i = 0; i < subWords.size(); i++) {
            final String currentSubWord = subWords.get(i);
            final float stringWidth = getStringWidth(font, fontSize, currentSubWord);
            if (stringWidth > charLimit) {
                if (previousBrokenWord.equals(currentSubWord)) {
                    continue;
                }
                previousBrokenWord = currentSubWord;
                subWords.remove(currentSubWord);
                final List<String> brokenStrings = breakWrapString(font, fontSize, currentSubWord, charLimit);
                subWords.addAll(i, brokenStrings);
            }
        }

        final List<String> finalStrings = new ArrayList<>();
        String currentStringCombo = "";
        // combine strings if possible
        for (final String currentSubWord : subWords) {
            if (StringUtils.isBlank(currentSubWord)) {
                continue;
            }
            if (getStringWidth(font, fontSize, currentStringCombo) + getStringWidth(font, fontSize, currentSubWord) > charLimit) {
                if (StringUtils.isBlank(currentStringCombo)) {
                    finalStrings.add(currentSubWord);
                } else {
                    finalStrings.add(currentStringCombo);
                    currentStringCombo = currentSubWord;
                }
            } else {
                if (StringUtils.isBlank(currentStringCombo)) {
                    currentStringCombo = currentSubWord;
                } else {
                    currentStringCombo += " " + currentSubWord;
                }
            }
        }
        if (currentStringCombo.length() > 0) {
            finalStrings.add(currentStringCombo);
        }

        return finalStrings;
    }

    public static float getStringWidth(final String text) throws IOException {
        final String fixedText = replaceUnsupportedCharacters(text);
        final float rawLength = PDFBoxManager.DEFAULT_FONT.getStringWidth(fixedText);
        return rawLength * (PDFBoxManager.DEFAULT_FONT_SIZE / 960f);
    }

    public static float getStringWidth(final PDFont font, final float fontSize, final String text) throws IOException {
        final String fixedText = replaceUnsupportedCharacters(text);
        final float rawLength = font.getStringWidth(fixedText);
        return rawLength * (fontSize / 960f);
    }

    public static List<String> breakWrapString(final PDFont font, final float fontSize, final String str, final int charLimit) throws IOException {
        int lastBreak = 0;
        float maxLengthCounter = 0;
        final int strLen = str.length();
        final ArrayList<String> brokenUpStrings = new ArrayList<>();
        // break up strings on non alphanumeric IF POSSIBLE
        for (int i = 1; i < strLen; i++) {
            if (!StringUtils.isAlphanumeric(str.charAt(i) + "") || maxLengthCounter >= charLimit) {
                brokenUpStrings.add(str.substring(lastBreak, i));
                lastBreak = i;
                maxLengthCounter = 0;
            } else {
                maxLengthCounter = getStringWidth(font, fontSize, str.substring(lastBreak, i));
            }
        }
        // add remaining string to the list so nothing gets lost
        if (brokenUpStrings.isEmpty() || (maxLengthCounter > 0 && maxLengthCounter < charLimit)) {
            brokenUpStrings.add(str.substring(lastBreak, strLen));
        }

        final List<String> finalStrings = new ArrayList<>();
        String currentStringCombo = "";
        // combine broken pieces if they will fit within the limit
        for (final String currentBrokenString : brokenUpStrings) {
            if (getStringWidth(font, fontSize, currentStringCombo) + getStringWidth(font, fontSize, currentBrokenString) > charLimit) {
                finalStrings.add(currentStringCombo);
                currentStringCombo = currentBrokenString;
            } else {
                currentStringCombo += currentBrokenString;
            }
        }
        if (currentStringCombo.length() > 0) {
            finalStrings.add(currentStringCombo);
        }

        return finalStrings;
    }

    public static String replaceUnsupportedCharacters(final String text) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if (WinAnsiEncoding.INSTANCE.contains(text.charAt(i))) {
                builder.append(text.charAt(i));
            } else {
                builder.append("?");
            }
        }
        return builder.toString();
    }
}