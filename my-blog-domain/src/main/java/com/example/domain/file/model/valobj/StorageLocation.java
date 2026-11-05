package com.example.domain.file.model.valobj;

/** 完整存储定位，不包含访问域名或临时签名。 */
public record StorageLocation(StorageType storageType, String bucketName, String storageKey) {
}
