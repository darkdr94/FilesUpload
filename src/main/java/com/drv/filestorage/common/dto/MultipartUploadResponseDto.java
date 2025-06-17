package com.drv.filestorage.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MultipartUploadResponseDto {
    private String key;
    private String uploadId;
    private List<PartInfoResponseDto> urls;
}
