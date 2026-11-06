package com.example.domain.file.service;

import com.example.domain.file.adapter.port.FileTypePolicy;
import org.springframework.stereotype.Component;

/** 首版允许上传任意文件类型。 */
@Component
public class AllowAllFileTypePolicy implements FileTypePolicy {

    /** {@inheritDoc} */
    @Override
    public void validate(String originalName, String mimeType) {
    }
}
