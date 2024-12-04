package intelligentcurtainwall.ossmanagement.controller;

import com.aliyun.core.utils.StringUtils;
import intelligentcurtainwall.ossmanagement.service.OssService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/oss")
public class OssController {

    private final OssService ossService;

    private final AuthenticationController authenticationController;

    @Value("${oss.bucket}")
    private String bucket;

    @Autowired
    public OssController(OssService ossService, AuthenticationController authenticationController) {
        this.ossService = ossService;
        this.authenticationController = authenticationController;
    }

    @GetMapping("/download/**")
    public ResponseEntity<byte[]> downloadFile(HttpServletRequest request) {
        // Get the full URI and extract the object key
        String fullPath = request.getRequestURI().replace(request.getContextPath(), "");
        String key = fullPath.substring("/oss/download/".length());

        // Fetch the file content from OSS
        byte[] fileContent = ossService.getObject(bucket, key);
        if (fileContent == null) {
            return ResponseEntity.notFound().build();
        }

        // Extract the file name from the key
        String fileName = key.substring(key.lastIndexOf("/") + 1);

        // Set the content disposition header for file download
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        return ResponseEntity.ok().headers(headers).body(fileContent);
    }

    @PostMapping("/upload/**")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
                                             @RequestParam("userName") String userName,
                                             @RequestParam("password") String password,
                                             HttpServletRequest request) {
        // Authenticate user
        AuthenticationController.AuthenticationRequest authRequest = new AuthenticationController.AuthenticationRequest(userName, password);
        AuthenticationController.AuthenticationResponse authResponse = authenticationController.authenticate(authRequest);

        // Check if authentication failed
        if (StringUtils.isEmpty(authResponse.accessKeyId()) || StringUtils.isEmpty(authResponse.accessKeySecret())) {
            return ResponseEntity.status(401).body("Authentication failed: Invalid username or password");
        }

        // Generate object key for the uploaded file
        String fullPath = request.getRequestURI().replace(request.getContextPath(), "");
        String objectKey = userName + '/' + fullPath.substring("/oss/upload/".length());

        // Validate objectKey
        if (!isValidObjectKey(objectKey)) {
            return ResponseEntity.status(400).body("Invalid object key format: Directories must contain only [A-Za-z0-9-] and filenames must contain only [A-Za-z0-9-.]");
        }

        // Upload file to OSS
        try {
            String uploadedFileKey = ossService.putObject(bucket, objectKey, file);
            String downloadUrl = "http://110.42.214.164:8005/oss/download/" + uploadedFileKey;
            return ResponseEntity.ok(downloadUrl);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("File upload failed: " + e.getMessage());
        }
    }

    private boolean isValidObjectKey(String objectKey) {
        if (objectKey == null || objectKey.isEmpty() || objectKey.endsWith("/")) {
            return false;
        }
        String[] parts = objectKey.split("/");
        for (int i = 0; i < parts.length - 1; i++) {
            if (!parts[i].matches("^[A-Za-z0-9-]+$")) {
                return false;
            }
        }
        String fileName = parts[parts.length - 1];
        if (!fileName.matches("^[A-Za-z0-9-.]+$")) {
            return false;
        }
        return !objectKey.contains("//");
    }
}
