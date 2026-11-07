package com.example.trigger.http;

import cn.hutool.core.util.StrUtil;
import com.example.domain.file.model.dto.FileUploadResponseDTO;
import com.example.domain.file.model.entity.BlogFileEntity;
import com.example.domain.file.service.IFileService;
import com.example.types.core.R;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URLConnection;

import static com.example.types.constant.FileUploadConstants.ALLOWED_MIME_TYPES;
import static com.example.types.constant.FileUploadConstants.MAX_FILENAME_LENGTH;

@RestController
@RequestMapping("/admin/files")
@RequiredArgsConstructor
public class BlogFileController {

    private final IFileService fileService;

    /**
     * 上传一个文件，并返回完成元数据入库的文件信息。
     *
     * @param file 上传的文件
     * @return 文件上传结果
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<FileUploadResponseDTO> uploadFile(@RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        String filename = cleanFilename(file.getOriginalFilename());
        String mimeType = detectMimeType(file);
        if (StrUtil.isBlank(mimeType) || !ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new IllegalArgumentException("仅支持上传 PNG、JPEG 和 GIF 图片");
        }
        BlogFileEntity uploadedFile = fileService.upload(filename, file.getSize(), mimeType, file::getInputStream);
        String objectKey = uploadedFile.getStorageKey();
        FileUploadResponseDTO response = FileUploadResponseDTO.builder()
                .fileId(uploadedFile.getId())
                .objectKey(objectKey)
                .url("/" + objectKey)
                .originalName(uploadedFile.getOriginalName())
                .mimeType(uploadedFile.getMimeType())
                .fileSize(uploadedFile.getFileSize())
                .build();
        return R.success(response);
    }

    private String cleanFilename(String originalName) {
        if (StrUtil.isBlank(originalName)) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        int lastSeparator = Math.max(originalName.lastIndexOf('/'), originalName.lastIndexOf('\\'));
        String filename = originalName.substring(lastSeparator + 1).replaceAll("\\p{Cntrl}", "").strip();
        if (StrUtil.isBlank(filename)) {
            throw new IllegalArgumentException("文件名不合法");
        }
        if (filename.codePointCount(0, filename.length()) > MAX_FILENAME_LENGTH) {
            throw new IllegalArgumentException("文件名不能超过 255 个字符");
        }
        return filename;
    }

    private String detectMimeType(MultipartFile file) {
        try (var stream = new BufferedInputStream(file.getInputStream())) {
            return URLConnection.guessContentTypeFromStream(stream);
        } catch (IOException e) {
            throw new IllegalArgumentException("无法读取上传文件", e);
        }
    }
}
