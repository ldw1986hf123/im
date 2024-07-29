package com.kuailu.im.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kuailu.im.core.dto.FileDto;
import com.kuailu.im.core.http.AjaxResult;
import com.kuailu.im.server.enums.YesOrNoEnum;
import com.kuailu.im.server.mapper.FileMapper;
import com.kuailu.im.server.model.ResponseModel;
import com.kuailu.im.server.model.entity.File;
import com.kuailu.im.server.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 *
 */
@Service
@Slf4j
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements FileService {

    @Value("${file.path}")
    private String filePath;

    @Value("${file.baseUrl}")
    private String fileBaseUrl;

    @Override
    public AjaxResult upload(MultipartFile file, String fullName, String suffix, String sender, String receiver, Long fileSize, Long duration) {
        AjaxResult result = AjaxResult.fail();

        String relaPath = DateUtil.today() + "/" + sender;
        String fullPath = filePath + relaPath;
        Boolean uploadOK = uploadFile(file, fullPath, fullName);

        //content就是前端访问的连接
        String escapedFullName = generateUrl(fullName);
        String content = fileBaseUrl + relaPath + "/" + escapedFullName;

        if (!uploadOK) {
            return result.fail("文件保存失败");
        }

        File savedFile = new File();
        savedFile.setFileSize(fileSize)
                .setDuration(duration)
                .setContent(content)
                .setSuffix(suffix)
                .setFullName(fullName)
                .setReceiver(receiver)
                .setStatus(YesOrNoEnum.YES.getCode())
                .setCreatedBy(sender);
        savedFile.setCreatedTime(new Date());
        Boolean successSaved = save(savedFile);
        if (!successSaved) {
            log.error("上传成功，但是保存到数据库失败.savedFile:  {}", JSON.toJSONString(savedFile));
            return result.fail("上传成功，但是保存到数据库失败");
        }
        FileDto fileDto = BeanUtil.copyProperties(savedFile, FileDto.class);
        result.success(fileDto);
        return result;
    }

    @Override
    @Transactional
    public ResponseModel uploadFiles(MultipartFile[] file,
                                     List<String> fileNames,
                                     List<String> suffixes,
                                     List<Long> fileSizes,
                                     String sender,
                                     String receiver, Long duration,
                                     Integer orientation) {
        ResponseModel result = ResponseModel.error();

        List<MultipartFile> multipartFileList = Arrays.asList(file);
        List<FileDto> retFileDtoList = new ArrayList<>();

        String relaPath = sender + "__" + receiver;
        String fullPath = filePath + relaPath;

        Boolean successSaved = false;

        for (int index = 0; index < multipartFileList.size(); index++) {
            MultipartFile originalFile = file[index];
            String suffix = suffixes.get(index);
            Long fileSize = fileSizes.get(index);
            String fineName = index + "_" + fileNames.get(index);
            Boolean uploadOK = uploadFile(originalFile, fullPath, fineName);

            //content就是前端访问的连接
            String escapedFullName = generateUrl(fineName);
            String content = fileBaseUrl + relaPath + "/" + escapedFullName;

            if (!uploadOK) {
                return result.error("文件保存失败");
            }

            File savedFile = new File();
            savedFile.setFileSize(fileSize)
                    .setDuration(duration)
                    .setContent(content)
                    .setSuffix(suffix)
                    .setFullName(fineName)
                    .setReceiver(receiver)
                    .setSender(sender)
                    .setStatus(YesOrNoEnum.YES.getCode())
                    .setOrientation(orientation)
                    .setCreatedBy(sender);
            savedFile.setCreatedTime(new Date());
            successSaved = save(savedFile);

            if (successSaved) {
                FileDto fileDto = BeanUtil.copyProperties(savedFile, FileDto.class);
                retFileDtoList.add(fileDto);
            } else {
                log.error("上传成功，但是保存到数据库失败.savedFile:  {}", JSON.toJSONString(savedFile));
            }
        }
        /************************************上传次文件***********************************/

        return result.success(retFileDtoList);
    }


    /**
     * 按日期每天生成一个文件夹，每个用户id生成一个文件夹
     * todo 需要比较文件是否是同一个
     *
     * @param file
     * @return
     */
    private Boolean uploadFile(MultipartFile file, String fullPath, String fileName) {
        Boolean uploadOK = false;
        try {
            if (!FileUtil.exist(fullPath)) {
                FileUtil.mkdir(fullPath);
            }
            file.transferTo(new java.io.File(fullPath + "//" + fileName));
            uploadOK = true;
        } catch (IOException e) {
            log.error("保存失败", e);
        }
        return uploadOK;
    }


    public static String generateUrl(String fileName) {
        // 对文件名进行 URL 编码
        String encodedFileName = "";
        try {
            encodedFileName = URLEncoder.encode(fileName, "UTF-8");
            encodedFileName = encodedFileName.replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            log.error("文件名转义失败fileName:{}", fileName, e);
        }
        // 拼接完整的 URL 地址
        return encodedFileName;
    }


    public String escape(String input) {
        if (input == null) {
            return null;
        }

        StringBuilder output = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (isChinese(ch)) {
                output.append("//" + Integer.toHexString(ch));
            } else {
                output.append(ch);
            }
        }

        return output.toString();
    }

    private static boolean isChinese(char ch) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(ch);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
                ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS ||
                ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A ||
                ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B ||
                ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION ||
                ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS ||
                ub == Character.UnicodeBlock.GENERAL_PUNCTUATION;
    }


}
