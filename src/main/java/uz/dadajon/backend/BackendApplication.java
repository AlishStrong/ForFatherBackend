package uz.dadajon.backend;

import java.net.InetSocketAddress;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@SpringBootApplication
@RestController
public class BackendApplication {

	@Autowired
	private SshService sshService;

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@GetMapping("/test")
	public String test(ServerHttpRequest request) {
		InetSocketAddress remoteAddress = request.getRemoteAddress();
		String requestersIP = remoteAddress.getAddress().getHostAddress();
		return requestersIP;
	}

	@GetMapping("/mono")
	public Mono<String> testMono() {
		return Mono.just("Mono test exe");
	}
	
	
	@PostMapping("/geo")
	public String receiveGeolocation(@RequestBody Position position) {
		System.out.println("Received a position");
		System.out.println(position);
		return "Roger Roger!";
	}

	// attach IP of the requester
	// log all request and responses to a file
    // create a separate log file for each day
    // log file name: [date].log
    // log format: time requester-IP requested-MSISDN unique-ID-of-each-request/response status: requested, responded, error
	@GetMapping("/command10")
	public Mono<ResponseEntity<Object>> getMethodName(@RequestParam long msisdn, ServerHttpRequest request) {
		InetSocketAddress remoteAddress = request.getRemoteAddress();
		String requestersIP = remoteAddress.getAddress().getHostAddress();
		LocalTime requestTime = LocalTime.now();

		return sshService.executeLaunchers(msisdn)
			.map(ctailResponse -> ResponseEntity.ok().body((Object) ctailResponse))
			.onErrorReturn(ResponseEntity.badRequest().body((Object) "no data"));
	}
}
