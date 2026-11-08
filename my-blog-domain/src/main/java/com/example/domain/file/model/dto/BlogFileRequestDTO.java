package com.example.domain.file.model.dto;

import com.example.domain.file.model.valobj.FileType;
import lombok.Data;

@Data
public class BlogFileRequestDTO {

    private String searchKeyword;

    private FileType fileType;
}
