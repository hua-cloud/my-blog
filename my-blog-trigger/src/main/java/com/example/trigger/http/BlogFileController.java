package com.example.trigger.http;

import com.example.domain.file.model.dto.FileUploadResult;
import com.example.domain.file.service.FileService;
import com.example.types.core.R;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.WebUtils;

import java.util.List;

@RestController
@RequestMapping("/admin/files")
@RequiredArgsConstructor
public class BlogFileController {

    private final FileService fileService;

    /**
     * 上传一个文件，并返回完成元数据入库的文件信息。
     *
     * @param request 包含一个名为 file 的文件部分的请求
     * @return 文件上传结果
     */
    @PostMapping("/upload")
    public R<FileUploadResult> upload(HttpServletRequest request) {
        MultipartHttpServletRequest multipart = WebUtils.getNativeRequest(request, MultipartHttpServletRequest.class);
        if (multipart == null || !MediaType.MULTIPART_FORM_DATA.includes(MediaType.parseMediaType(request.getContentType()))) {
            throw new IllegalArgumentException("请使用 multipart/form-data 上传文件");
        }
        List<MultipartFile> files = multipart.getFiles("file");
        if (files.isEmpty()) {
            throw new IllegalArgumentException("请选择一个文件");
        }
        if (files.size() != 1 || multipart.getMultiFileMap().size() != 1) {
            throw new IllegalArgumentException("只允许上传一个文件");
        }
        MultipartFile file = files.get(0);
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        return R.success(fileService.upload(file.getOriginalFilename(), file.getSize(), file::getInputStream));
    }
}
