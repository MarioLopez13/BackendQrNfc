package com.kynsof.share.core.application.FileServices;

public interface IFileUploadService {
    UploadResponse uploadJasperToS3(byte[] jasperBytes, String fileName, String folderPath);
}
