package com.kuailu.im.server.controller;

import com.kuailu.im.core.http.AjaxResult;
import com.kuailu.im.server.model.ResponseModel;
import com.kuailu.im.server.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @description: 文件接口
 */

@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Autowired
    FileService fileService;

    /**
     * 最大不能超过 500M
     *
     * @param file
     * @param fullName
     * @param suffix
     * @param fileSize
     * @param sender
     * @param receiver
     * @param duration
     * @return
     */
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam(value = "file") MultipartFile file,
                             @RequestParam(value = "fullName", required = false) String fullName,
                             @RequestParam(value = "suffix", required = false) String suffix,
                             @RequestParam(value = "fileSize", required = false) Long fileSize,
                             @RequestParam(value = "sender", required = false) String sender,
                             @RequestParam(value = "receiver", required = false) String receiver,
                             @RequestParam(value = "duration", required = false) Long duration) {
        AjaxResult result = AjaxResult.fail();
     /*   if (null == file || file.getSize() == 0) {
            return result.fail("file不能为空");
        }
        if (StringUtils.isEmpty(fullName)) {
            return result.fail("文件全名不能为空");
        }
        if (StringUtils.isEmpty(suffix)) {
            return result.fail("后缀名不能为空");
        }
        if (StringUtils.isEmpty(sender)) {
            return result.fail("发送人id不能为空");
        }
        if (StringUtils.isEmpty(receiver)) {
            return result.fail("接收人id不能为空");
        }*/
        //todo duration 也要校验
        result = fileService.upload(file, fullName, suffix, sender, receiver, fileSize, duration);
        return result;
    }

    /**
     * 最大不能超过 500M
     *
     * @param file
     * @param fullName
     * @param suffix
     * @param fileSize
     * @param sender
     * @param receiver
     * @param duration
     * @return
     */
    @PostMapping("/uploadFiles")
    public ResponseModel uploadFiles(@RequestParam(value = "file") MultipartFile[] file,
                                     @RequestParam(value = "fullName", required = false) List<String> fullNames,
                                     @RequestParam(value = "suffix", required = false) List<String> suffixes,
                                     @RequestParam(value = "fileSize", required = false) List<Long> fileSizes,
                                     @RequestParam(value = "sender", required = false) String sender,
                                     @RequestParam(value = "receiver", required = false) String receiver,
                                     @RequestParam(value = "duration", required = false) Long duration,
                                     @RequestParam(value = "orientation", required = false) Integer orientation) {
        ResponseModel result = ResponseModel.error();

        //todo duration 也要校验
        result = fileService.uploadFiles(file, fullNames, suffixes, fileSizes, sender, receiver,duration,orientation);
        return result;
    }


}
