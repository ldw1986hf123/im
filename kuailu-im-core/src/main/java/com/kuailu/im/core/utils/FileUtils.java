package com.kuailu.im.core.utils;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Properties;

/**
 * @description:
 * @author: 林坚丁
 * @time: 2023/1/16 14:25
 */
@Slf4j
public class FileUtils {
    private    static String jarDirPath="";
    public  static String getJarPath() {
        if(jarDirPath=="") {
            String path = FileUtil.getAbsolutePath("");
            String separator = "/";
            if (path.contains("\\")) {
                separator = "\\";
            }
            if (path.contains(".jar")) {
                path = path.substring(0, path.lastIndexOf(".jar"));
                path = path.substring(0, path.lastIndexOf(separator) + 1);
            }
            if (path.contains("classes")) {
                path = path.replace("/classes", "");
            }
            jarDirPath=path;
        }

        return jarDirPath;
    }
}
