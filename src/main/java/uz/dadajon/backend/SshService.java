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

    public String executeCtail(long phonenumber) {
        String output = null;

        SshClient sshClient = SshClient.setUpDefaultClient();
        sshClient.start();

        try {
            ConnectFuture sshConnectFuture = sshClient.connect(username, host, port);
            ClientSession clientSession = sshConnectFuture.verify(10, TimeUnit.SECONDS).getSession();
            clientSession.addPasswordIdentity(username);
            clientSession.auth().verify(10, TimeUnit.SECONDS);

            String ctailCmd = String.format("%s %s %d", pythonExe, ctailPath, phonenumber);
            ChannelExec channelExec = clientSession.createExecChannel(ctailCmd);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            channelExec.setOut(out);
            channelExec.setErr(err);

            channelExec.open();
            Set<ClientChannelEvent> events = channelExec.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), TimeUnit.SECONDS.toMillis(10));
            clientSession.close(false);
        
            // Check if timed out
            if (events.contains(ClientChannelEvent.TIMEOUT)) {
                System.out.println("There was TIMEOUT exception");
            }

            output = out.toString();
            String error = err.toString();
            Integer statusCode = channelExec.getExitStatus();

            System.out.println("CTAIL: Output was - " + output);
            System.out.println("CTAIL: Error was - " + error);
            System.out.println("CTAIL: Status Code was - " + statusCode);
        } catch (Exception e) {
            System.out.println("SSH Client exception");
            System.out.println(e);
        } finally {
            sshClient.stop();;
        }

        if (Objects.nonNull(output)) {
            System.out.println("CTAIL Return OUTPUT - " + output + " at - " + LocalDateTime.now());

            // attach IP of the requester
            // Current output form:
            // Info | 19:43:20.672 SORM_CLASS [900]RSP->(buf)[01] CC 01 23 30 01 00 01 00 01 FF CB 21 01 01 0C 99 98 30 72 61 57 FF FF FF FF FF 01 01 02 00 01 0F 34 04 14 01 85 03 43 F5 FF 10 53 74 40 71 81 94 42 10 FF 0A 30 43 B4 FB 92 01 at - 2025-01-01T19:43:22.029618600

            //  New output from python in JSON string
            //  { time: , switch: , msisdn: , imsi: , imei: , mnc: , cellId: , lac: }

            // log all request and responses to a file
            // create a separate log file for each day
            // log file name: [date].log
            // log format: time requester-IP requested-MSISDN unique-ID-of-each-request/response status: requested, responded, error
            return output + " at - " + LocalDateTime.now();
        } else {
            return "CTAIL no output";
        }
    }

    public void executeLauncher(long phonenumber) {
        SshClient sshClient = SshClient.setUpDefaultClient();
        sshClient.start();

        try {
            ConnectFuture sshConnectFuture = sshClient.connect(username, host, port);
            ClientSession clientSession = sshConnectFuture.verify(1, TimeUnit.SECONDS).getSession();
            clientSession.addPasswordIdentity(username);
            clientSession.auth().verify(1, TimeUnit.SECONDS);

            String ctailCmd = String.format("%s %s %d", pythonExe, launcherPath, phonenumber);
            ChannelExec channelExec = clientSession.createExecChannel(ctailCmd);

            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ByteArrayOutputStream err = new ByteArrayOutputStream();
                channelExec.setOut(out);
                channelExec.setErr(err);
    
                channelExec.open();
                Set<ClientChannelEvent> events = channelExec.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), TimeUnit.SECONDS.toMillis(1));
                clientSession.close(false);
            
                // Check if timed out
                if (events.contains(ClientChannelEvent.TIMEOUT)) {
                    System.out.println("Execute Launcher faced a TIMEOUT exception");
                }
    
                String output = out.toString();
                String error = err.toString();
                Integer statusCode = channelExec.getExitStatus();
    
                System.out.println("LAUNCHER: Output was - " + output);
                System.out.println("LAUNCHER: Error was - " + error);
                System.out.println("LAUNCHER: Status Code was - " + statusCode);
            } finally {
                channelExec.close();
            }
        } catch (Exception e) {
            System.out.println("SSH Client exception - " + e.getMessage());
            e.printStackTrace();
        } finally {
            sshClient.stop();
        }
    }
}
