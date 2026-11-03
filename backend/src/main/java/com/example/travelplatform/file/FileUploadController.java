package com.example.travelplatform.file;

import com.example.travelplatform.common.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@CrossOrigin
public class FileUploadController {

    private static final String UPLOAD_DIR = "uploads";

    // 上传接口不再做权限拦截，由前端控制谁可以操作
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> upload(@RequestPart("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ApiResponse.error(400, "文件为空");
        }

        Files.createDirectories(Paths.get(UPLOAD_DIR));

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String ext = "";
        int dot = originalFilename.lastIndexOf('.');
        if (dot >= 0) {
            ext = originalFilename.substring(dot);
        }
        String filename = UUID.randomUUID() + ext;
        Path targetPath = Paths.get(UPLOAD_DIR).resolve(filename);
        Files.copy(file.getInputStream(), targetPath);

        String url = "/uploads/" + filename;
        return ApiResponse.success(url);
    }
}

