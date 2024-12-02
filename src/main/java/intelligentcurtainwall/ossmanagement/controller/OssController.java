package intelligentcurtainwall.ossmanagement.controller;

import intelligentcurtainwall.ossmanagement.service.OssService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class OssController {

    private final OssService ossService;

    @Value("${aliyun.oss.bucket}")
    private String bucket;

    @Autowired
    public OssController(OssService ossService) {
        this.ossService = ossService;
    }

    @GetMapping("/oss/**")
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
}
