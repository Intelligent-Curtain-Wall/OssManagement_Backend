package intelligentcurtainwall.ossmanagement.service;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class SshService {

    public InputStream[] executeCommand(String host, int port, String username, String password, String command) throws Exception {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        InputStream inputStream = channel.getInputStream();
        InputStream errorStream = channel.getErrStream();
        channel.connect();

        return new InputStream[]{inputStream, errorStream};
    }
}
