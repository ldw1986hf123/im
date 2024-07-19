package com.kuailu.im.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kuailu.im.core.http.AjaxResult;
import com.kuailu.im.server.model.ResponseModel;
import com.kuailu.im.server.model.entity.File;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService extends IService<File> {
    AjaxResult upload(MultipartFile file,
                      String fullName,
                      String suffix,
                      String sender,
                      String receiver,
                      Long fileSize,
                      Long duration);

    ResponseModel uploadFiles(MultipartFile[] file,
                              List<String> fullNames,
                              List<String> suffixes,
                              List<Long> fileSizes,
                              String sender,
                              String receiver,
                              Long duration,
                              Integer orientation);

}
