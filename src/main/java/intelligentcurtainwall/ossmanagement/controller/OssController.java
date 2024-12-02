package intelligentcurtainwall.ossmanagement.controller;

import intelligentcurtainwall.ossmanagement.service.OssService;
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

    @GetMapping("/oss/{key}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String key) {
        byte[] fileContent = ossService.getObject(bucket, key);
        String fileName = key.substring(key.lastIndexOf("/") + 1);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        return ResponseEntity.ok().headers(headers).body(fileContent);
    }
}
