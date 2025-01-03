package uz.dadajon.backend;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

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
	public String test() {
		return "Classic test endpoint exe";
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


	// There can be several servers and in each server Ctail-Launcher needs to be executed
	// {"currentTime":"22:31:42.063","switchNumber":"900","msisdn":"998903271675","imsi":"434041105830345","imei":"3547041718492401","mnc":"03","lac":"5E97","cellId":"9EC"}
	@GetMapping("/command10")
	public Mono<String> getMethodName(@RequestParam long msisdn) {
		List<Long> phonenumbers = Arrays.asList(998909997433L, 998937904486L, msisdn);
		System.out.println("ALL STARTED AT - " + LocalDateTime.now());

		return phonenumbers.stream().map(pn -> {
			Mono<String> ctailMono = Mono.fromCallable(() -> {
				System.out.println("=== Calling CTAIL for " + pn + " at - " + LocalDateTime.now());
				return sshService.executeCtail(msisdn);
			}).subscribeOn(Schedulers.boundedElastic());
	
			Mono<String> launcherMono = Mono.delay(Duration.ofMillis(1000)).map(tick -> {
				System.out.println("--- Calling LAUNCHER for " + pn + " at - " + LocalDateTime.now());
				return sshService.executeLauncher(msisdn);
			});

			return Mono.zip(ctailMono, launcherMono).map(tuple -> {
				return "Result for number " + pn + " -- " + tuple.getT1();
			});
			
		}).collect(Collectors.collectingAndThen(Collectors.toList(), monosList -> Mono.zip(monosList, Tuples.fnAny()))).map(tuple -> {
			System.out.println("++++++++++++++++++++++++++++++");
			System.out.println(tuple);
			return "check logs";
		});




	}
}
