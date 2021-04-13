/*
 * integration-reporting
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.pdf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;

public abstract class JarResourceCopier {
    public List<File> copy(final String resourceDir, final String destinationDir) throws IOException, URISyntaxException {
        final List<String> fileList = findRelativePathFileList();
        return writeFiles(fileList, resourceDir, destinationDir);
    }

    public abstract List<String> findRelativePathFileList();

    private List<File> writeFiles(final List<String> fileList, final String resourceDir, final String destinationDir) throws IOException {
        final List<File> writtenList = new LinkedList<>();
        for (final String relativePath : fileList) {
            final String resourceFile = resourceDir + relativePath;
            final String destFile = destinationDir + File.separator + relativePath;
            if (!copyFileViaClass(resourceFile, destFile, writtenList)) {
                copyFileViaClassLoader(resourceFile, destFile, writtenList);
            }
        }
        return writtenList;
    }

    private boolean copyFileViaClass(final String resourcePath, final String destFile, final List<File> writtenFileList) throws IOException {
        try (InputStream resourceStream = getClassInputStream(resourcePath)) {
            if (resourceStream == null) {
                return false;
            } else {
                copyFile(resourceStream, destFile, writtenFileList);
                return true;
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private boolean copyFileViaClassLoader(final String resourcePath, final String destFile, final List<File> writtenFileList) throws IOException {
        try (InputStream resourceStream = getClassLoaderInputStream(resourcePath)) {
            if (resourceStream == null) {
                return false;
            } else {
                copyFile(resourceStream, destFile, writtenFileList);
                return true;
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void copyFile(final InputStream resourceStream, final String destFile, final List<File> writtenFileList) throws IOException {
        final File filePath = new File(destFile);
        filePath.getParentFile().mkdirs();
        Files.copy(resourceStream, filePath.toPath(), StandardCopyOption.REPLACE_EXISTING);
        writtenFileList.add(filePath);
    }

    private InputStream getClassLoaderInputStream(final String resourcePath) {
        return this.getClass().getClassLoader().getResourceAsStream(resourcePath);
    }

    private InputStream getClassInputStream(final String resourcePath) {
        return this.getClass().getResourceAsStream(resourcePath);
    }

}
