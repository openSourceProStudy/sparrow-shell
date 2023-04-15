/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sparrow.utility;

import com.sparrow.constant.File;
import com.sparrow.cryptogram.Base64;
import com.sparrow.io.file.FileNameProperty;
import com.sparrow.protocol.constant.Constant;
import com.sparrow.protocol.constant.Extension;
import com.sparrow.protocol.constant.magic.Digit;
import com.sparrow.protocol.constant.magic.Symbol;
import com.sparrow.support.EnvironmentSupport;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtility {
    private static Logger logger = LoggerFactory.getLogger(FileUtility.class);
    private static final FileUtility FILE_UTILITY = new FileUtility();

    private FileUtility() {
    }

    public static FileUtility getInstance() {
        return FILE_UTILITY;
    }

    public String readFileContent(String fileName) {
        return readFileContent(fileName, null);
    }

    /**
     * 以行为单位读取文件，常用于读面向行的格式化文件
     */
    public String readFileContent(String fileName, String charset) {
        if (StringUtility.isNullOrEmpty(charset)) {
            charset = StandardCharsets.UTF_8.name();
        }
        InputStream inputStream = null;
        try {
            inputStream = EnvironmentSupport.getInstance().getFileInputStream(fileName);
        } catch (FileNotFoundException e) {
            logger.error("[{}] not found", fileName);
            return null;
        }
        return this.readFileContent(inputStream, charset);
    }

    /**
     * 以行为单位读取文件，常用于读面向行的格式化文件
     */
    public String readFileContent(InputStream inputStream, String charset) {
        if (StringUtility.isNullOrEmpty(charset)) {
            charset = Constant.CHARSET_UTF_8;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = -1;
        try {
            while ((length = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, length);
            }
            return bos.toString(charset);
        } catch (IOException e) {
            logger.error("write error", e);
            return Symbol.EMPTY;
        } finally {
            try {
                bos.close();
                inputStream.close();
            } catch (IOException ignore) {
            }
        }
    }

    public List<String> readLines(String fileName) {
        return this.readLines(fileName, Constant.CHARSET_UTF_8);
    }

    public List<String> readLines(InputStream inputStream) {
        return this.readLines(inputStream, Constant.CHARSET_UTF_8);
    }

    public List<String> readLines(InputStream inputStream, String charset) {
        if (inputStream == null) {
            return null;
        }

        List<String> fileLines = new ArrayList<String>();
        if (StringUtility.isNullOrEmpty(charset)) {
            charset = Constant.CHARSET_UTF_8;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    inputStream, charset));
            String tempString;
            while ((tempString = reader.readLine()) != null) {
                fileLines.add(tempString);
            }
        } catch (IOException e) {
            logger.error("flush error", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.error("close error", e);
                }
            }
        }
        return fileLines;
    }

    public List<String> readLines(String fileName, String charset) {
        if (StringUtility.isNullOrEmpty(charset)) {
            charset = Constant.CHARSET_UTF_8;
        }
        InputStream inputStream = null;
        try {
            inputStream = EnvironmentSupport.getInstance().getFileInputStream(fileName);
        } catch (FileNotFoundException e) {
            logger.error("[{}] file not found", fileName);
            return null;
        }
        return this.readLines(inputStream, charset);
    }

    private String makeDirectory(String fullFilePath) {
        java.io.File destFile = new java.io.File(fullFilePath);
        if (destFile.isDirectory()) {
            return fullFilePath;
        }
        //如果不存在目录，则放至家目录
        String descDirectoryPath = this.getFileNameProperty(fullFilePath).getDirectory();
        if (StringUtility.isNullOrEmpty(descDirectoryPath)) {
            descDirectoryPath = System.getProperty("user.dir");
            fullFilePath = descDirectoryPath + java.io.File.separator + fullFilePath;
        }
        // 判断并创建原图路径
        java.io.File descDirectory = new java.io.File(descDirectoryPath);
        if (!descDirectory.exists()) {
            if (descDirectory.mkdirs()) {
                return fullFilePath;
            }
        }
        return fullFilePath;
    }

    public void writeFile(String filePath, String s) throws IOException {
        this.writeFile(filePath, s, StandardCharsets.UTF_8.name());
    }

    public void writeFile(String filePath, String s, String charset) throws IOException {
        OutputStreamWriter osw = null;
        try {
            FileNameProperty fileNameProperty = this.getFileNameProperty(filePath);
            if (!new java.io.File(fileNameProperty.getDirectory()).exists()) {
                //不存在则新建class以外的目录
                filePath = this.makeDirectory(filePath);
            }
            OutputStream outputStream = Files.newOutputStream(Paths.get(filePath));
            osw = new OutputStreamWriter(outputStream,
                    charset);
            osw.write(s, Digit.ZERO, s.length());
            osw.flush();
        } catch (Exception e) {
            logger.error("flush error", e);
            throw e;
        } finally {
            if (osw != null) {
                try {
                    osw.close();
                } catch (IOException e) {
                    logger.error("close error", e);
                }
            }
        }
    }

    /**
     * 文件复制
     *
     * @param descFilePath
     * @param srcFilePath
     */
    public void copy(String srcFilePath, String descFilePath) {
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            String fileFullPath = this.makeDirectory(descFilePath);
            java.io.File descFile = new java.io.File(fileFullPath);
            if (descFile.isDirectory()) {
                descFile = new java.io.File(fileFullPath + java.io.File.separator + this.getFileNameProperty(srcFilePath).getFullFileName());
            }
            is = Files.newInputStream(Paths.get(srcFilePath));
            fos = new FileOutputStream(descFile);
            byte[] buffer = new byte[Digit.K];
            while (true) {
                int readSize = is.read(buffer);
                if (readSize == -1) {
                    break;
                }
                fos.write(buffer, Digit.ZERO, readSize);
            }

        } catch (Exception e) {
            throw new RuntimeException("file not found", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    logger.error("input stream error", e);
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    logger.error("output stream error", e);
                }
            }
        }
    }

    /**
     * 文件复制
     *
     * @param inputStream
     * @param outputStream
     */
    public void copy(InputStream inputStream, OutputStream outputStream) {
        if (inputStream == null || outputStream == null) {
            return;
        }
        try {
            byte[] buffer = new byte[Digit.K];
            while (true) {
                int readSize = inputStream.read(buffer);
                if (readSize == -1) {
                    break;
                }
                outputStream.write(buffer, Digit.ZERO, readSize);
            }
            if (outputStream instanceof ZipOutputStream) {
                ((ZipOutputStream) outputStream).finish();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.error("input stream close error", e);
            }
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                logger.error("output stream close error", e);
            }
        }
    }

    public String search(String path, String keyword, int skip,
                         Comparator<String> compare, int minSkip) {
        java.io.File file = new java.io.File(path);

        BufferedReader reader = null;
        try {
            if (!file.exists() && !file.createNewFile()) {
                throw new FileNotFoundException(file.getPath());
            }
            reader = new BufferedReader(new InputStreamReader(
                    Files.newInputStream(file.toPath()), StandardCharsets.UTF_8), skip);
            String tempString;
            reader.mark(skip);
            skip /= 2;
            reader.skip(skip);
            reader.readLine();
            boolean isSkip = true;
            String lastWord = Symbol.EMPTY;
            while ((tempString = reader.readLine()) != null) {
                if (compare.compare(tempString, keyword) == 0) {
                    return tempString;
                } else if (tempString.equals(lastWord)) {
                    return Symbol.EMPTY;
                }
                // 是否需要skip
                if (!isSkip) {
                    continue;
                }
                // 往外跳
                if (compare.compare(tempString, keyword) < Digit.ZERO) {
                    // 先做标记
                    reader.mark(skip);
                    // 如果不足标准skip ，跳跃浮度减半
                    if (skip < skip / Digit.TOW) {
                        skip /= Digit.TOW;
                    } else {
                        // 跳出文本范围 则减半
                        while (reader.skip(skip) < skip) {
                            skip /= Digit.TOW;
                            reader.reset();
                        }
                    }
                    reader.readLine();
                    // 向里跳
                } else {
                    reader.reset();
                    skip /= Digit.TOW;
                    if (skip <= minSkip) {
                        isSkip = false;
                        lastWord = tempString;
                    } else {
                        reader.skip(skip);
                        reader.readLine();
                    }
                }
            }
            return Symbol.EMPTY;
        } catch (IOException e) {
            logger.error("reade file error", e);
            return Symbol.EMPTY;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    logger.error("reader close error", e1);
                }
            }
        }
    }

    /**
     * 根据单位为BYTE的长度获取文件长度（可变单位为MB GB）
     *
     * @param length 单位是B
     * @return
     */
    public String getHumanReadableFileLength(Long length) {
        if (length == null) {
            return Symbol.EMPTY;
        }

        if (length < Digit.K) {
            return length + "B";
        }
        double kb = length / 1024D;//for double result

        if (kb >= Digit.ONE && kb < Digit.K) {
            return Math.ceil(kb) + "KB";
        }
        // 四舍五入保留两位小数
        if (kb >= Digit.K && kb < Math.pow(Digit.K, Digit.TOW)) {
            return Math.ceil(kb / Digit.K * Digit.K) / Digit.K + "MB";
        }
        // 四舍五入保留两位小数
        return Math.ceil(kb / Digit.K / Digit.K * Digit.HUNDRED) / Digit.HUNDRED + "GB";
    }

    /**
     * 兼容 <a href="http://www.sparrozoo.com/bmiddle/003aGgFyzy6IXdCe80y52">...</a>
     *
     * @param fullFilePath 文件的全路径
     * @return FileNameProperty
     */
    public FileNameProperty getFileNameProperty(String fullFilePath) {
        FileNameProperty fileNameProperty = new FileNameProperty();
        /**
         * /target/apidocs/package-list  is not file path
         */
        java.io.File file = new java.io.File(fullFilePath);
        if (file.isDirectory()) {
            fileNameProperty.setDirectory(true);
            fileNameProperty.setDirectory(fullFilePath);
            return fileNameProperty;
        }
        fileNameProperty.setDirectory(false);
        int lastFileSeparatorIndex = fullFilePath.lastIndexOf(java.io.File.separator);
        if (lastFileSeparatorIndex == -1) {
            lastFileSeparatorIndex = fullFilePath.lastIndexOf("/");
        }
        String directory = fullFilePath.substring(Digit.ZERO, lastFileSeparatorIndex + Digit.ONE);
        fileNameProperty.setDirectory(directory);
        fileNameProperty.setFullFileName(fullFilePath);


        int fileNameEndIndex = fullFilePath.lastIndexOf('.');
        if (fileNameEndIndex < lastFileSeparatorIndex) {
            /**
             * /target/apidocs/package-list
             */
            fileNameProperty.setName(fullFilePath.substring(lastFileSeparatorIndex + Digit.ONE));
            logger.error("extension of file '{}' not exist  ", fullFilePath);
            return fileNameProperty;
        }
        String fileName = fullFilePath.substring(lastFileSeparatorIndex + Digit.ONE,
                fileNameEndIndex);
        String extension = fullFilePath.substring(fileNameEndIndex)
                .toLowerCase();
        String fullFileName = fileName + extension;
        fileNameProperty.setDirectory(directory);
        fileNameProperty.setFullFileName(fullFileName);
        fileNameProperty.setName(fileName);
        String imageExtensionConfig = ConfigUtility.getValue(File.IMAGE_EXTENSION);
        if (imageExtensionConfig == null) {
            imageExtensionConfig = Constant.IMAGE_EXTENSION;
        }
        String[] imageExtension = imageExtensionConfig
                .split("\\|");
        // jpeg 或者是其他格式都转换成jpg
        if (Extension.JPEG.equalsIgnoreCase(extension)) {
            extension = Extension.JPG;
        }
        fileNameProperty.setExtension(extension);
        fileNameProperty.setImage(StringUtility.existInArray(imageExtension, extension));
        return fileNameProperty;
    }

    /**
     * 通过一数字ID获取文件打散路径
     *
     * @param fileUuid
     * @param extension
     * @param isWebPath
     * @param size
     * @return
     */
    public String getShufflePath(String fileUuid, String extension, boolean isWebPath,
                                 String size) {
        CRC32 crc = new CRC32();
        crc.update(fileUuid.getBytes());
        long id = crc.getValue();
        boolean isImage = this.isImage(extension);
        long remaining = id % Digit.TWELVE;
        long remaining1 = id % Digit.THOUSAND;
        long div = id / Digit.THOUSAND;
        long remaining2 = div % Digit.THOUSAND;
        String path;
        if (isImage) {
            if (isWebPath) {
                //img url 中存在参数
                //img_url=http://img%1$s/sparrowzoo.net/%2$s/%3$s/%4$s/%5$s%6$s
                //http://img1/sparrowzoo.net/1/big/10/1000/uuid.jpg
                path = ConfigUtility.getValue(File.PATH.IMG_URL)
                        + "/%2$s/%3$s/%4$s/%5$s%6$s";
                return String.format(path, remaining, size, remaining2, remaining1,
                        fileUuid, extension);
            }
            //img_unc_0=file://ip1:port/sparrow/img0 参数在key中定义
            String imgShufflerDir = ConfigUtility.getValue(File.PATH.IMG_SHUFFLER_DIR + "_" + remaining);
            path = imgShufflerDir
                    + "/%1$s/%2$s/%3$s/%4$s%5$s";
            return String.format(path, size, remaining2, remaining1,
                    fileUuid, extension);
        }
        path = ConfigUtility.getValue(File.PATH.FILE_UPLOAD)
                + "/%1$s/%2$s/%3$s%4$s";
        return String.format(path, remaining2, remaining1, fileUuid, extension);
    }

    public boolean isImage(String extension) {
        String imageExtension = ConfigUtility
                .getValue(File.IMAGE_EXTENSION);
        if (StringUtility.isNullOrEmpty(imageExtension)) {
            imageExtension = Constant.IMAGE_EXTENSION;
        }
        return StringUtility.existInArray(imageExtension.split("\\|"), extension);
    }

    /**
     * 根据站内附件url获取物理路径
     *
     * @param filePath
     * @param size
     * @return
     */
    public String getPhysicalPath(String filePath, String size) {
        FileNameProperty fileNameProperty = this.getFileNameProperty(filePath);
        String fileId = fileNameProperty.getName();
        String extension = fileNameProperty.getExtension();
        return this.getShufflePath(fileId, extension, false,
                size);
    }

    public void delete(String path, long beforeMillis) {
        java.io.File file = new java.io.File(path);
        if (!file.isDirectory()) {
            logger.error("{} is not directory", path);
            return;
        }
        java.io.File[] files = file.listFiles();
        if (files == null) {
            return;
        }
        // 如果没有文件
        if (files.length == Digit.ZERO) {
            boolean result = file.delete();
            logger.info("deleted file {},result:{}", path, result);
            return;
        }
        for (java.io.File f : files) {
            if (f.isDirectory() && !f.isHidden()) {
                delete(f.getPath(), beforeMillis);
                continue;
            }
            if (f.lastModified() < beforeMillis) {
                boolean result = f.delete();
                logger.info("deleted file {},result:{}", path, result);

            }
        }
    }

    public boolean existLine(String fileName, String line) {
        List<String> lineList = this.readLines(fileName);
        return StringUtility.existInArray(lineList.toArray(), line);
    }

    public String replacePath(String fullPath, String source, String destination) {
        return replacePath(fullPath, source, destination, null);
    }

    public String replacePath(String fullPath, String source, String destination, String separator) {
        if (StringUtility.isNullOrEmpty(separator)) {
            separator = java.io.File.separator;
        }

        String[] splits = fullPath.split(Symbol.DOT.equals(separator) ? "\\." : separator);
        for (int i = 0; i < splits.length; i++) {
            if (splits[i].equalsIgnoreCase(source)) {
                splits[i] = destination;
                break;
            }
        }
        return StringUtility.join(separator, splits);
    }

    /**
     * 删除文件
     */
    public void deleteByFileId(String fileUuid, String clientFileName) {
        FileNameProperty fileNameProperty = this.getFileNameProperty(clientFileName);
        String extension = fileNameProperty.getExtension();
        Boolean isImage = fileNameProperty.isImage();
        boolean result;
        if (isImage) {
            String imageFullPath = FileUtility.getInstance().getShufflePath(
                    fileUuid,
                    extension, false, File.SIZE.ORIGIN);
            java.io.File origin = new java.io.File(imageFullPath);
            if (origin.exists()) {
                result = origin.delete();
                logger.info("deleted file {},result:{}", imageFullPath, result);
            }

            java.io.File big = new java.io.File(imageFullPath.replace(File.SIZE.ORIGIN,
                    File.SIZE.BIG));
            if (big.exists()) {
                result = big.delete();
                logger.info("deleted file {},result:{}", big.getAbsolutePath(), result);
            }

            java.io.File middle = new java.io.File(imageFullPath.replace(File.SIZE.ORIGIN,
                    File.SIZE.MIDDLE));
            if (middle.exists()) {
                result = middle.delete();
                logger.info("deleted file {},result:{}", middle.getAbsolutePath(), result);
            }

            java.io.File small = new java.io.File(imageFullPath.replace(File.SIZE.ORIGIN,
                    File.SIZE.SMALL));
            if (small.exists()) {
                result = small.delete();
                logger.info("deleted file {},result:{}", imageFullPath, result);
            }
            return;
        }
        String attachFileFullName = FileUtility.getInstance().getShufflePath(
                fileUuid, extension, false, File.SIZE.ATTACH);
        java.io.File origin = new java.io.File(attachFileFullName);
        if (origin.exists()) {
            result = origin.delete();
            logger.info("deleted file {},result:{}", attachFileFullName, result);
        }
    }

    public boolean generateImage(String base64str,
                                 String savePath) {
        if (base64str == null) {
            return false;
        }
        FileNameProperty fileNameProperty = this.getFileNameProperty(savePath);
        java.io.File directory = new java.io.File(fileNameProperty.getDirectory());
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                logger.error("create directory error {}", directory);
                return false;
            }
        }
        try {
            byte[] b = Base64.decode(base64str);
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            OutputStream out = Files.newOutputStream(Paths.get(savePath));
            out.write(b);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            logger.error("image generate fail {}", base64str);
            return false;
        }
    }

    public interface FileCopier {
        void copy(String sourceFile, String targetFile);
    }

    public interface FolderFilter {
        Boolean filter(String sourceFile, String targetFile);
    }

    public void recurseCopy(String source, String target) {
        recurseCopy(source, target, null, null);
    }

    public void recurseCopy(String source, String target, FileCopier fileCopier, FolderFilter folderFilter) {
        java.io.File sourceFile = new java.io.File(source);
        if (!sourceFile.exists()) {
            logger.error("{} source is not exist", source);
            return;
        }
        if (sourceFile.isFile()) {
            //也可以转换为字符串，进行字符串的替换操作
            if (fileCopier != null) {
                fileCopier.copy(source, target);
            } else {
                FileUtility.getInstance().copy(source, target);
            }
            return;
        }
        //获取目录下的所有文件
        java.io.File[] files = sourceFile.listFiles();

        if (files == null || files.length == 0) {
            logger.error("{} source is empty", source);
            return;
        }

        String parent = target;
        for (java.io.File f : files) {
            source = f.toString();
            target = parent + java.io.File.separator + f.getName();
            if (f.isDirectory() && fileCopier != null && folderFilter.filter(source, target)) {
                logger.error("directory {} is not copy", f.getAbsolutePath());
                continue;
            }
            if (f.isFile()) {
                recurseCopy(source, target, fileCopier, folderFilter);
                continue;
            }

            //准备进行目录的创建
            java.io.File mkFile = new java.io.File(target);
            if (!mkFile.exists()) {
                //先将目录创建出来
                boolean result = mkFile.mkdirs();
                logger.info("created {},result:{}", target, result);
            }
            //递归调用
            recurseCopy(source, target, fileCopier, folderFilter);
        }
    }
}