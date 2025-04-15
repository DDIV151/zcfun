package com.ddiv.zcfun.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadResult {
    @JsonProperty(value = "file_name")
    private String fileName;
    @JsonProperty(value = "file_id")
    private String fileUrl;
    @JsonProperty(value = "file_type")
    private String fileType;
    @JsonProperty(value = "file_size")
    private long fileSize;
}
