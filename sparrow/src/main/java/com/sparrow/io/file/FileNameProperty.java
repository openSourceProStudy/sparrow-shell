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

package com.sparrow.io.file;

public class FileNameProperty {
    private Boolean isDirectory;
    /**
     * 文件名
     */
    private String name;
    /**
     * 文件扩展名
     */
    private String extension;
    /**
     * 文件扩展名不带点
     */
    private String extensionWithoutDot;
    /**
     * 文件所在目录
     */
    private String directory;
    /**
     * 文件全名
     */
    private String fullFileName;
    /**
     * 是否是图片
     */
    private Boolean image;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getFullFileName() {
        return fullFileName;
    }

    public void setFullFileName(String fullFileName) {
        this.fullFileName = fullFileName;
    }

    public Boolean isImage() {
        return image;
    }

    public void setImage(Boolean image) {
        this.image = image;
    }

    public String getExtensionWithoutDot() {
        if (extensionWithoutDot == null) {
            this.extensionWithoutDot = this.extension.substring(1);
        }
        return extensionWithoutDot;
    }

    public void setDirectory(Boolean directory) {
        isDirectory = directory;
    }

    public Boolean isDirectory() {
        return isDirectory;
    }

    @Override
    public String toString() {
        return "FileNameProperty{" +
                "isDirectory=" + isDirectory +
                ", name='" + name + '\'' +
                ", extension='" + extension + '\'' +
                ", extensionWithoutDot='" + extensionWithoutDot + '\'' +
                ", directory='" + directory + '\'' +
                ", fullFileName='" + fullFileName + '\'' +
                ", image=" + image +
                '}';
    }
}
