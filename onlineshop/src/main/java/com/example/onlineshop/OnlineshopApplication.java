package com.example.onlineshop;

import com.example.onlineshop.model.entity.User;
import com.example.onlineshop.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;

import java.util.Random;

import static com.example.onlineshop.model.entity.User.Role.OWNER;

@AllArgsConstructor
@SpringBootApplication
public class OnlineshopApplication {

	UserRepository userRepository;
	PasswordEncoder passwordEncoder;
	private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

	@PostConstruct
	public void initAdmin(){
		User admin = userRepository.findByUsername("admin").orElse(new User("admin", "admin", "", OWNER));
		Random random = new Random();
		String dict = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		String password = random.ints(0, dict.length())
				.limit(40)
				.collect(StringBuilder::new, (str, ind)-> str.append(dict.charAt(ind)), StringBuilder::append)
				.toString();

		admin.setPassword(passwordEncoder.encode(password));
		userRepository.save(admin);
		System.out.println("----------------------------------------------------------------\n\nCurrent admin password: " +
				password+"\n\n----------------------------------------------------------------");
		for (Session i: sessionRepository.findByPrincipalName("admin").values()){
			sessionRepository.deleteById(i.getId());
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(OnlineshopApplication.class, args);
	}

}
