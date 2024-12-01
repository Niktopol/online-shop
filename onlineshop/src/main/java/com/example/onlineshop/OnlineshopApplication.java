package com.example.onlineshop;

import com.example.onlineshop.model.entity.User;
import com.example.onlineshop.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.example.onlineshop.model.entity.User.Role.OWNER;

@AllArgsConstructor
@SpringBootApplication
public class OnlineshopApplication {

	UserRepository repo;
	PasswordEncoder enc;

	@PostConstruct
	public void addOwner(){
		try{
			repo.save(new User("own", "own",
					enc.encode("123"), OWNER, true));
		}catch (Exception ignored){
        }
	}

	public static void main(String[] args) {
		SpringApplication.run(OnlineshopApplication.class, args);
	}

}
