package intelligentcurtainwall.ossmanagement.controller;

import intelligentcurtainwall.ossmanagement.service.SshService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

@RestController
public class DockerLogsController {

    private final SshService sshService;
    private final Properties sshConfig;

    public DockerLogsController(SshService sshService) {
        this.sshService = sshService;
        this.sshConfig = new Properties();
        try {
            sshConfig.load(getClass().getResourceAsStream("/ssh-config.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping(value = "/docker-logs/{containerId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamDockerLogs(@PathVariable String containerId) {
        final SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        new Thread(() -> {
            try {
                String command = "echo " + sshConfig.getProperty("password") + " | sudo -S docker logs -f " + containerId;
                InputStream[] streams = sshService.executeCommand(
                        sshConfig.getProperty("host"),
                        Integer.parseInt(sshConfig.getProperty("port")),
                        sshConfig.getProperty("username"),
                        sshConfig.getProperty("password"),
                        command);
                InputStream in = streams[0];
                InputStream err = streams[1];

                BufferedReader stdIn = new BufferedReader(new InputStreamReader(in));
                BufferedReader stdErr = new BufferedReader(new InputStreamReader(err));

                String line;
                while ((line = stdIn.readLine()) != null) {
                    emitter.send(line + "\n");
                }
                while ((line = stdErr.readLine()) != null) {
                    emitter.send(line + "\n");
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }
}
