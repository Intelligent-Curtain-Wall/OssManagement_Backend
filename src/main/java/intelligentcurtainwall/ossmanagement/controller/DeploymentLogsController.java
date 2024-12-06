package intelligentcurtainwall.ossmanagement.controller;

import intelligentcurtainwall.ossmanagement.service.SshService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@RestController
public class DeploymentLogsController {

    private final SshService sshService;

    private final Properties sshConfig;

    public DeploymentLogsController(SshService sshService) {
        this.sshService = sshService;
        this.sshConfig = new Properties();
        try {
            sshConfig.load(getClass().getResourceAsStream("/ssh-config.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping(value = "/deployment-logs", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getDeploymentLogs() {
        String currentDate = getCurrentDate();
        String logDir = "/home/mat/Intelligent_Curtain_Wall/deployment-logs/" + currentDate;
        String command = "ls -t " + logDir + "/*.txt | head -n 1";
        String logFilePath = executeSSHCommand(command);
        if (logFilePath.isEmpty()) {
            return "No log file found for today.";
        }
        command = "cat " + logFilePath;
        return executeSSHCommand(command);
    }

    private String getCurrentDate() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
    }

    private String executeSSHCommand(String command) {
        try {
            InputStream[] streams = sshService.executeCommand(
                    sshConfig.getProperty("host"),
                    Integer.parseInt(sshConfig.getProperty("port")),
                    sshConfig.getProperty("username"),
                    sshConfig.getProperty("password"),
                    command
            );

            BufferedReader reader = new BufferedReader(new InputStreamReader(streams[0], StandardCharsets.UTF_8));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(streams[1], StandardCharsets.UTF_8));
            StringBuilder errorOutput = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }

            reader.close();
            errorReader.close();

            if (!errorOutput.isEmpty()) {
                return "Error: " + errorOutput;
            }

            return output.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
