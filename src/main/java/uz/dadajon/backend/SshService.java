package uz.dadajon.backend;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    @Value("${path.to.ctail}")
    private String ctailPath;

    @Value("${path.to.launcher}")
    private String launcherPath;

    @Value("${python.executable}")
    private String pythonExe;

    public String executeLauncher(long phoneNumber) {
        executeCommand(String.format("%s %s %d", pythonExe, launcherPath, phoneNumber));
        return "launcher launched";
    }


    // attach IP of the requester
    // Current output form:
    // Info | 19:43:20.672 SORM_CLASS [900]RSP->(buf)[01] CC 01 23 30 01 00 01 00 01 FF CB 21 01 01 0C 99 98 30 72 61 57 FF FF FF FF FF 01 01 02 00 01 0F 34 04 14 01 85 03 43 F5 FF 10 53 74 40 71 81 94 42 10 FF 0A 30 43 B4 FB 92 01 at - 2025-01-01T19:43:22.029618600

    //  New output from python in JSON string
    //  { time: , switch: , msisdn: , imsi: , imei: , mnc: , cellId: , lac: }

    // log all request and responses to a file
    // create a separate log file for each day
    // log file name: [date].log
    // log format: time requester-IP requested-MSISDN unique-ID-of-each-request/response status: requested, responded, error
    public String executeCtail(long phoneNumber) {
        String output = executeCommand(String.format("%s %s %d", pythonExe, ctailPath, phoneNumber));
        return processOutput(output);
    }

    private String executeCommand(String command) {
        SshClient sshClient = SshClient.setUpDefaultClient();
        sshClient.start();
        String output = null;

        try {
            ConnectFuture sshConnectFuture = sshClient.connect(username, host, port);
            ClientSession clientSession = sshConnectFuture.verify(10, TimeUnit.SECONDS).getSession();
            clientSession.addPasswordIdentity(password); // Ensure you have the password variable
            clientSession.auth().verify(10, TimeUnit.SECONDS);

            ChannelExec channelExec = clientSession.createExecChannel(command);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            channelExec.setOut(out);
            channelExec.setErr(err);

            channelExec.open();
            Set<ClientChannelEvent> events = channelExec.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), TimeUnit.SECONDS.toMillis(10));
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
        } catch (Exception e) {
            System.err.println("SSH Client exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            sshClient.stop(); // Ensure the SSH client is stopped
        }

        return output;
    }

    private String processOutput(String output) {
        if (Objects.nonNull(output)) {
            String logMessage = "CTAIL Return OUTPUT - " + output + " at - " + LocalDateTime.now();
            System.out.println(logMessage);
            return output + " at - " + LocalDateTime.now();
        } else {
            return "CTAIL no output";
        }
    }
}
