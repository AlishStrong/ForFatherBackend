package uz.dadajon.backend;

import java.io.ByteArrayOutputStream;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SshService {

    @Value("${ssh.username}")
    private String username;

    @Value("${ssh.password}")
    private String password;

    @Value("${ssh.host}")
    private String host;

    @Value("${ssh.port}")
    private Integer port;

    @Value("${path.to.launcher}")
    private String launcherPath;

    @Value("${python.executable}")
    private String pythonExe;

    // log all request and responses to a file
    // create a separate log file for each day
    // log file name: [date].log
    // log format: time requester-IP requested-MSISDN unique-ID-of-each-request/response status: requested, responded, error
    public CtailResponse executeLauncher(long phoneNumber) throws Exception {
        SshClient sshClient = SshClient.setUpDefaultClient();
        sshClient.start();
        String output = null;

        ConnectFuture sshConnectFuture = sshClient.connect(username, host, port);
        ClientSession clientSession = sshConnectFuture.verify(10, TimeUnit.SECONDS).getSession();
        clientSession.addPasswordIdentity(password); // Ensure you have the password variable
        clientSession.auth().verify(10, TimeUnit.SECONDS);

        String command = String.format("%s %s %d", pythonExe, launcherPath, phoneNumber);
        ChannelExec channelExec = clientSession.createExecChannel(command);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        channelExec.setOut(out);
        channelExec.setErr(err);

        channelExec.open();
        Set<ClientChannelEvent> events = channelExec.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), TimeUnit.SECONDS.toMillis(20));
        clientSession.close(false);

        // Check if timed out
        if (events.contains(ClientChannelEvent.TIMEOUT)) {
            System.err.println("Execution timed out.");
        }

        output = out.toString();
        String error = err.toString();
        Integer statusCode = channelExec.getExitStatus();

        System.out.println("Command Output: " + output);
        System.out.println("Command Error: " + error);
        System.out.println("Command Status Code: " + statusCode);

        sshClient.stop(); // Ensure the SSH client is stopped

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(output, CtailResponse.class);
    }
}
