/**
 * integration-reporting
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;

public class PDFBoxManager implements Closeable {
    public static final PDFont DEFAULT_FONT = PDType1Font.HELVETICA;
    public static final PDFont DEFAULT_FONT_BOLD = PDType1Font.HELVETICA_BOLD;
    public static final float DEFAULT_FONT_SIZE = 10;
    public static final Color DEFAULT_COLOR = Color.BLACK;
    public final File outputFile;
    public final PDDocument document;
    public PDPage currentPage;
    private PDPageContentStream contentStream;

    public PDFBoxManager(final File outputFile, final PDDocument document) throws IOException {
        this.outputFile = outputFile;
        this.document = document;
        this.currentPage = new PDPage();
        document.addPage(currentPage);
        contentStream = new PDPageContentStream(document, currentPage, AppendMode.APPEND, true, false);
    }

    public PDRectangle drawRectangleCentered(final float x, final float cellUpperY, final float width, final float height, final float cellHeight, final Color color) throws IOException {
        return drawRectangle(x - (width / 2), cellUpperY - (cellHeight / 2) - (height / 2), width, height, color);
    }

    public PDRectangle drawRectangle(final float x, final float y, final float width, final float height, final Color color) throws IOException {
        final float startingY = checkYAndSwitchPage(y, height);
        contentStream.setNonStrokingColor(color);
        contentStream.addRect(x, startingY, width, height);
        contentStream.fill();
        return new PDRectangle(x, startingY, width, height);
    }

    public PDRectangle drawImageCentered(final float x, final float cellUpperY, final float width, final float height, final float cellWidth, final float cellHeight, final String resourceImageName) throws IOException, URISyntaxException {
        return drawImage(x - (cellWidth / 2), cellUpperY - (cellHeight / 2) - (height / 2), width, height, resourceImageName);
    }

    public PDRectangle drawImage(final float x, final float y, final float width, final float height, final String resourceImageName) throws IOException, URISyntaxException {
        final float startingY = checkYAndSwitchPage(y, height);
        final BufferedImage bufferedImage = ImageIO.read(getClass().getResourceAsStream(resourceImageName));
        final PDImageXObject pdImage = LosslessFactory.createFromImage(document, bufferedImage);
        contentStream.drawImage(pdImage, x, startingY, width, height);
        return new PDRectangle(x, startingY, width, height);
    }

    public PDRectangle writeTextCentered(final float x, final float y, final String text, final PDFont font, final float fontSize, final Color textColor) throws IOException {
        final float textLength = StringManager.getStringWidth(font, fontSize, text);
        return writeText(x - (textLength / 2), y, text, font, fontSize, textColor);
    }

    public PDRectangle writeTextCentered(final float x, final float y, final String text) throws IOException {
        return writeTextCentered(x, y, text, DEFAULT_FONT, DEFAULT_FONT_SIZE, DEFAULT_COLOR);
    }

    public PDRectangle writeTextCentered(final float x, final float cellUpperY, final float height, final String text, final PDFont font, final float fontSize, final Color textColor) throws IOException {
        final float textLength = StringManager.getStringWidth(font, fontSize, text);
        return writeText(x - (textLength / 2), cellUpperY - (height / 2) - (fontSize / 2), text, font, fontSize, textColor);
    }

    public PDRectangle writeTextCentered(final float x, final float y, final float height, final String text) throws IOException {
        return writeTextCentered(x, y, height, text, DEFAULT_FONT, DEFAULT_FONT_SIZE, DEFAULT_COLOR);
    }

    public PDRectangle writeText(final float x, final float y, final String text) throws IOException {
        return writeText(x, y, text, DEFAULT_FONT, DEFAULT_FONT_SIZE, DEFAULT_COLOR);
    }

    public PDRectangle writeText(final float x, final float y, final String text, final PDFont font, final float fontSize, final Color textColor) throws IOException {
        final float startingY = checkYAndSwitchPage(y, fontSize);
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.setNonStrokingColor(textColor);
        contentStream.newLineAtOffset(x, startingY);
        final String fixedText = StringManager.replaceUnsupportedCharacters(text);
        contentStream.showText(fixedText);
        contentStream.endText();
        return new PDRectangle(x, startingY, StringManager.getStringWidth(font, fontSize, fixedText), fontSize);
    }

    public PDRectangle writeWrappedText(final float x, final float y, final float width, final String text) throws IOException {
        return writeWrappedText(x, y, width, text, DEFAULT_FONT, DEFAULT_FONT_SIZE, DEFAULT_COLOR);
    }

    public PDRectangle writeWrappedCenteredText(final float x, final float cellUpperY, final float width, final float height, final List<String> textLines, final PDFont font, final float fontSize, final Color color) throws IOException {
        final float lowestY = checkYAndSwitchPage(cellUpperY - height, fontSize);
        final int numOfLines = textLines.size();
        final int centerOfText = numOfLines / 2;
        float actualWidth = width;
        float leftMostX = x + width;
        float centerY = cellUpperY - (height / 2) - (fontSize / 2);
        if (numOfLines % 2 == 0) {
            centerY -= fontSize / 2;
        }
        for (int i = 0; i < numOfLines; i++) {
            final float textLength = StringManager.getStringWidth(font, fontSize, textLines.get(i));
            final float textX = x - (textLength / 2);
            if (textX < leftMostX) {
                leftMostX = textX;
            }
            float textY = 0F;
            final int difference = Math.abs(i - centerOfText);
            if (i < centerOfText) {
                textY = centerY + (difference * fontSize);
            } else {
                textY = centerY - (difference * fontSize);
            }
            final PDRectangle rectangle = writeText(textX, textY, textLines.get(i), font, fontSize, color);
            if (numOfLines == 1) {
                actualWidth = rectangle.getWidth();
            }
        }
        return new PDRectangle(leftMostX, lowestY, actualWidth, height);
    }

    public PDRectangle writeWrappedVerticalCenteredText(final float x, final float cellUpperY, final float width, final float height, final List<String> textLines) throws IOException {
        return writeWrappedVerticalCenteredText(x, cellUpperY, width, height, textLines, DEFAULT_FONT, DEFAULT_FONT_SIZE, DEFAULT_COLOR);
    }

    public PDRectangle writeWrappedVerticalCenteredText(final float x, final float cellUpperY, final float width, final float height, final List<String> textLines, final PDFont font, final float fontSize, final Color color)
            throws IOException {
        final float lowestY = checkYAndSwitchPage(cellUpperY - height, fontSize);

        final int numOfLines = textLines.size();
        final int centerOfText = numOfLines / 2;
        float actualWidth = width;
        float centerY = cellUpperY - (height / 2) - fontSize / 3;
        if (numOfLines % 2 == 0) {
            centerY -= fontSize / 2;
        }
        for (int i = 0; i < numOfLines; i++) {
            float textY = 0F;
            final int difference = Math.abs(i - centerOfText);
            if (i < centerOfText) {
                textY = centerY + (difference * fontSize);
            } else {
                textY = centerY - (difference * fontSize);
            }
            final PDRectangle rectangle = writeText(x, textY, textLines.get(i), font, fontSize, color);
            if (numOfLines == 1) {
                actualWidth = rectangle.getWidth();
            }
        }

        return new PDRectangle(x, lowestY, actualWidth, height);
    }

    public PDRectangle writeWrappedText(final float x, final float y, final float width, final String text, final PDFont font, final float fontSize, final Color color) throws IOException {
        final List<String> textLines = StringManager.wrapToCombinedList(font, fontSize, text, Math.round(width));
        return writeWrappedText(x, y, width, textLines, font, fontSize, color);
    }

    public PDRectangle writeWrappedText(final float x, final float y, final float width, final List<String> textLines) throws IOException {
        return writeWrappedText(x, y, width, textLines, DEFAULT_FONT, DEFAULT_FONT_SIZE, DEFAULT_COLOR);
    }

    public PDRectangle writeWrappedText(final float x, final float y, final float width, final List<String> textLines, final PDFont font, final float fontSize, final Color color) throws IOException {
        final float startingY = checkYAndSwitchPage(y, fontSize);
        final int numOfLines = textLines.size();
        float actualWidth = width;
        float approximateHeight = 0F;
        float lowestY = startingY;
        for (int i = 0; i < numOfLines; i++) {
            final float textY = startingY - (i * fontSize);
            if (textY < lowestY) {
                lowestY = textY;
            }
            final PDRectangle rectangle = writeText(x, textY, textLines.get(i), font, fontSize, color);
            if (numOfLines == 1) {
                actualWidth = rectangle.getWidth();
            }
            approximateHeight += rectangle.getHeight();
        }
        return new PDRectangle(x, lowestY, actualWidth, approximateHeight);
    }

    public PDRectangle writeLink(final float x, final float y, final String linkText, final String linkURL, final PDFont font, final float fontSize) throws IOException {
        final PDRectangle rectangle = writeText(x, y, linkText, font, fontSize, Color.decode("#46759E"));
        addAnnotationLinkRectangle(rectangle.getLowerLeftX(), rectangle.getLowerLeftY(), rectangle.getWidth(), rectangle.getHeight(), linkURL);
        return rectangle;
    }

    public PDRectangle writeWrappedLink(final float x, final float y, final float width, final String linkText, final String linkURL, final PDFont font, final float fontSize) throws IOException {
        return writeWrappedLink(x, y, width, linkText, linkURL, font, fontSize, Color.decode("#46759E"));
    }

    public PDRectangle writeWrappedLink(final float x, final float y, final float width, final String linkText, final String linkURL, final PDFont font, final float fontSize, final Color color) throws IOException {
        final PDRectangle rectangle = writeWrappedText(x, y, width, linkText, font, fontSize, color);
        addAnnotationLinkRectangle(rectangle.getLowerLeftX(), rectangle.getLowerLeftY(), rectangle.getWidth(), rectangle.getHeight(), linkURL);
        return rectangle;
    }

    public PDRectangle writeWrappedVerticalCenteredLink(final float x, final float cellUpperY, final float width, final float height, final List<String> linkTextLines, final String linkURL, final Color color) throws IOException {
        return writeWrappedVerticalCenteredLink(x, cellUpperY, width, height, linkTextLines, linkURL, DEFAULT_FONT, DEFAULT_FONT_SIZE, color);
    }

    public PDRectangle writeWrappedVerticalCenteredLink(final float x, final float cellUpperY, final float width, final float height, final List<String> linkTextLines, final String linkURL, final PDFont font, final float fontSize,
            final Color color) throws IOException {
        final PDRectangle rectangle = writeWrappedVerticalCenteredText(x, cellUpperY, width, height, linkTextLines, font, fontSize, color);
        addAnnotationLinkRectangle(rectangle.getLowerLeftX(), rectangle.getLowerLeftY(), rectangle.getWidth(), rectangle.getHeight(), linkURL);
        return rectangle;
    }

    public PDRectangle writeWrappedLink(final float x, final float y, final float width, final List<String> linkTextLines, final String linkURL, final PDFont font, final float fontSize) throws IOException {
        return writeWrappedLink(x, y, width, linkTextLines, linkURL, font, fontSize, Color.decode("#46759E"));
    }

    public PDRectangle writeWrappedCenteredLink(final float x, final float rowUpperY, final float width, final float height, final List<String> linkTextLines, final String linkURL, final Color color) throws IOException {
        return writeWrappedCenteredLink(x, rowUpperY, width, height, linkTextLines, linkURL, DEFAULT_FONT, DEFAULT_FONT_SIZE, color);
    }

    public PDRectangle writeWrappedCenteredLink(final float x, final float rowUpperY, final float width, final float height, final List<String> linkTextLines, final String linkURL, final PDFont font, final float fontSize, final Color color)
            throws IOException {
        final PDRectangle rectangle = writeWrappedCenteredText(x, rowUpperY, width, height, linkTextLines, font, fontSize, color);
        addAnnotationLinkRectangle(rectangle.getLowerLeftX(), rectangle.getLowerLeftY(), rectangle.getWidth(), rectangle.getHeight(), linkURL);
        return rectangle;
    }

    public PDRectangle writeWrappedLink(final float x, final float y, final float width, final List<String> linkTextLines, final String linkURL, final Color color) throws IOException {
        return writeWrappedLink(x, y, width, linkTextLines, linkURL, DEFAULT_FONT, DEFAULT_FONT_SIZE, color);
    }

    public PDRectangle writeWrappedLink(final float x, final float y, final float width, final List<String> linkTextLines, final String linkURL, final PDFont font, final float fontSize, final Color color) throws IOException {
        final PDRectangle rectangle = writeWrappedText(x, y, width, linkTextLines, font, fontSize, color);
        addAnnotationLinkRectangle(rectangle.getLowerLeftX(), rectangle.getLowerLeftY(), rectangle.getWidth(), rectangle.getHeight(), linkURL);
        return rectangle;
    }

    private PDRectangle addAnnotationLinkRectangle(final float x, final float y, final float width, final float height, final String linkURL) throws IOException {
        final float startingY = checkYAndSwitchPage(y, height);
        final PDAnnotationLink txtLink = new PDAnnotationLink();
        final PDRectangle position = new PDRectangle();
        position.setLowerLeftX(x);
        position.setLowerLeftY(startingY);
        position.setUpperRightX(x + width);
        position.setUpperRightY(startingY + height);
        txtLink.setRectangle(position);

        final PDActionURI action = new PDActionURI();
        action.setURI(linkURL);
        txtLink.setAction(action);

        currentPage.getAnnotations().add(txtLink);
        return new PDRectangle(x, startingY, width, height);
    }

    private float checkYAndSwitchPage(final float y, final float height) throws IOException {
        if (y - 20 < 0) {
            contentStream.close();
            this.currentPage = new PDPage();
            document.addPage(currentPage);
            contentStream = new PDPageContentStream(document, currentPage, AppendMode.APPEND, true, false);
            return currentPage.getMediaBox().getHeight() - 20 - height;
        }
        return y;
    }

    public float getApproximateWrappedStringHeight(final int numberOfTextLines, final float fontSize) {
        return numberOfTextLines * fontSize + fontSize;
    }

    @Override
    public void close() throws IOException {
        contentStream.close();
        document.save(outputFile);
        document.close();
    }
}
