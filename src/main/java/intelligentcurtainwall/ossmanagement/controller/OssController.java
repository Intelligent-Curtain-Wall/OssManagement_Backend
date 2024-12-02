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

    @Value("${oss.bucket}")
    private String bucket;

    @Autowired
    private AuthenticationController authenticationController;

    @Autowired
    public OssController(OssService ossService) {
        this.ossService = ossService;
    }

    @GetMapping("/download/**")
    public ResponseEntity<byte[]> downloadFile(HttpServletRequest request) {
        String fullPath = request.getRequestURI().replace(request.getContextPath(), "");
        String key = fullPath.substring("/oss/".length());
        byte[] fileContent = ossService.getObject(bucket, key);
        if (fileContent == null) {
            return ResponseEntity.notFound().build();
        }
        String fileName = key.substring(key.lastIndexOf("/") + 1);
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

        // Upload file to OSS
        try {
            String uploadedFileKey = ossService.putObject(bucket, objectKey, file);
            String downloadUrl = "http://110.42.214.164:8005/oss/download/" + uploadedFileKey;
            return ResponseEntity.ok(downloadUrl);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("File upload failed: " + e.getMessage());
        }
    }
}
