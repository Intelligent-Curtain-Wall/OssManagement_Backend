package intelligentcurtainwall.ossmanagement.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class AuthenticationController {

    private final List<UserCredentials> users;

    public AuthenticationController() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        users = mapper.readValue(new ClassPathResource("oss-config.json").getInputStream(), new TypeReference<>() {
        });
    }

    @PostMapping("/authenticate")
    public AuthenticationResponse authenticate(@RequestBody AuthenticationRequest request) {
        for (UserCredentials user : users) {
            if (user.getUserName().equals(request.userName()) && user.getPassword().equals(request.password())) {
                return new AuthenticationResponse(user.getAccessKeyId(), user.getAccessKeySecret());
            }
        }
        return new AuthenticationResponse("", "");
    }

    public record AuthenticationRequest(String userName, String password) {
    }

    public record AuthenticationResponse(String accessKeyId, String accessKeySecret) {
    }

    private static class UserCredentials {
        @JsonProperty("UserName")
        private String userName;

        @JsonProperty("Password")
        private String password;

        @JsonProperty("AccessKeyId")
        private String accessKeyId;

        @JsonProperty("AccessKeySecret")
        private String accessKeySecret;

        public String getUserName() {
            return userName;
        }

        public String getPassword() {
            return password;
        }

        public String getAccessKeyId() {
            return accessKeyId;
        }

        public String getAccessKeySecret() {
            return accessKeySecret;
        }
    }
}