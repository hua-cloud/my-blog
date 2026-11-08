package com.example.trigger.http;

import cn.hutool.core.util.StrUtil;
import com.example.domain.file.model.dto.BlogFileRequestDTO;
import com.example.domain.file.model.dto.BlogFileResponseDTO;
import com.example.domain.file.model.entity.BlogFileEntity;
import com.example.domain.file.model.valobj.FileType;
import com.example.domain.file.service.IBlogFileService;
import com.example.types.core.PageQuery;
import com.example.types.core.PageResult;
import com.example.types.core.R;
import com.example.types.utils.MapstructUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URLConnection;

import static com.example.types.constant.Constants.BACK_SLASH;
import static com.example.types.constant.Constants.SLASH;
import static com.example.types.constant.FileUploadConstants.MAX_FILENAME_LENGTH;

@RestController
@RequestMapping("/admin/file")
@RequiredArgsConstructor
public class BlogFileController {

    private final IBlogFileService fileService;

    @GetMapping("/page")
    public R<PageResult<BlogFileResponseDTO>> page(BlogFileRequestDTO requestDTO, PageQuery pageQuery) {
        return R.success(fileService.page(requestDTO.getSearchKeyword(), requestDTO.getFileType(), pageQuery));
    }

    /**
     * 上传一个文件，并返回完成元数据入库的文件信息。
     *
     * @param file 上传的文件
     * @return 文件上传结果
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<BlogFileResponseDTO> uploadFile(@RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        String filename = cleanFilename(file.getOriginalFilename());
        String mimeType = detectMimeType(file);
        if (!FileType.supportsMimeType(mimeType)) {
            throw new IllegalArgumentException("仅支持上传 PNG、JPEG 和 GIF 图片");
        }
        BlogFileEntity fileEntity = fileService.upload(filename, file.getSize(), mimeType, file::getInputStream);
        return R.success(MapstructUtils.convert(fileEntity, BlogFileResponseDTO.class));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable("id") Long id) {
        return R.operation(fileService.delete(id));
    }

    private String cleanFilename(String originalName) {
        if (StrUtil.isBlank(originalName)) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        int lastSeparator = Math.max(originalName.lastIndexOf(SLASH), originalName.lastIndexOf(BACK_SLASH));
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
