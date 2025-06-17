package com.drv.filestorage.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PartInfoResponseDto {
    private int partNumber;
    private String presignedUrl;
}
