package uz.dadajon.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@SpringBootApplication
@RestController
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@GetMapping("/test")
	public String test() {
		return "test works";
	}
	
	@PostMapping("/geo")
	public String receiveGeolocation(@RequestBody Position position) {
		System.out.println("Received a position");
		System.out.println(position);
		return "Roger Roger!";
	}
}
