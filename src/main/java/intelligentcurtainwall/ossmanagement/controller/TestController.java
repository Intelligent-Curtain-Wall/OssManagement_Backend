package intelligentcurtainwall.ossmanagement.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/test")
    public String test() {
        return "智慧幕墙--对象存储OSS管理控制系统";
    }
}