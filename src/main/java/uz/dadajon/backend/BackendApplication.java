package uz.dadajon.backend;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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

	// http://host:2239/command10?msisdn=998903271675
	@GetMapping("/command10")
	public Mono<String> getMethodName(@RequestParam long msisdn) {
		System.out.println("ALL STARTED AT - " + LocalDateTime.now());
		// Mono<String> ctailMono = Mono.fromCallable(() -> {
		// 		try {
		// 			return ctailTest();
		// 		} catch (InterruptedException e) {
		// 			Thread.currentThread().interrupt();
		// 			return "Calling CTAIL exception at " + LocalDateTime.now();
		// 		}
		// 	}).subscribeOn(Schedulers.boundedElastic());

		// Mono<Void> launcherMono = Mono.delay(Duration.ofMillis(1000))
		// 	.doOnNext(tick -> {
		// 		System.out.println("= Calling LAUNCHER at - " + LocalDateTime.now());
		// 		launcherTest();
		// 	}).then();

		// launcherMono.subscribe();
		
		// return ctailMono;

		Mono<String> ctailMono = Mono.fromCallable(() -> {
			System.out.println("=== Calling CTAIL at - " + LocalDateTime.now());
			return sshService.executeCtail(msisdn);
		}).subscribeOn(Schedulers.boundedElastic());

		Mono<Void> launcherMono = Mono.delay(Duration.ofMillis(1000)).doOnNext(tick -> {
			System.out.println("--- Calling LAUNCHER at - " + LocalDateTime.now());
			sshService.executeLauncher(msisdn);
		}).then();

		launcherMono.subscribe();
		return ctailMono;
	}
}
