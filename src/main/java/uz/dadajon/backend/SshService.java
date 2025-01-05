package uz.dadajon.backend;

import java.io.ByteArrayOutputStream;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class SshService {

    @Value("${ssh.username}")
    private String username;

    @Value("${ssh.password}")
    private String password;

    @Value("#{'${ssh.hosts}'.split(';')}") 
    private List<String> hosts;

    @Value("${ssh.port}")
    private Integer port;

    @Value("${path.to.launcher}")
    private String launcherPath;

    @Value("${python.executable}")
    private String pythonExe;

    public Mono<CtailResponse> executeLaunchers(long phoneNumber) {
        return hosts.stream()
            .map((host) -> {
                return Mono.fromCallable(() -> executeLauncher(phoneNumber, host))
                            .subscribeOn(Schedulers.boundedElastic());
            })
            .collect(Collectors.collectingAndThen(Collectors.toList(), monosList -> Mono.firstWithValue(monosList)))
            .map(firstValue -> {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    return mapper.readValue(firstValue, CtailResponse.class);
                } catch (Exception e) {
                    return null;
                }
            });
    }

    // log all request and responses to a file
    // create a separate log file for each day
    // log file name: [date].log
    // log format: time requester-IP requested-MSISDN unique-ID-of-each-request/response status: requested, responded, error
    public String executeLauncher(long phoneNumber, String host) throws Exception {
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
            throw new Exception("SSH execution timed out for server - " + host);
        }

        output = out.toString().trim();
        Integer statusCode = channelExec.getExitStatus();

        if (statusCode > 0) {
            throw new Exception("SSH execution for server - " + host + " return non-zero status");
        }

        sshClient.stop(); // Ensure the SSH client is stopped

        if(output != null && !output.isEmpty()) {
            return output;
        } else {
            throw new Exception("No data for server - " + host);
        }
    }
}
