package app.user.service;

import app.exception.DomainException;
import app.subscription.service.SubscriptionService;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.property.UserProperty;
import app.user.repository.UserRepository;
import app.wallet.service.WalletService;
import app.web.dto.LoginRequest;
import app.web.dto.RegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionService subscriptionService;
    private final WalletService walletService;
    private final UserProperty userProperty;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, SubscriptionService subscriptionService, WalletService walletService, UserProperty userProperty) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.subscriptionService = subscriptionService;
        this.walletService = walletService;
        this.userProperty = userProperty;
    }

    public User login(LoginRequest loginRequest) {
        Optional<User> optionalUser=userRepository.findByUsername(loginRequest.getUsername());
        if(optionalUser.isEmpty()) {
            throw new DomainException("Username or password are incorrect.");
        }

        User user=optionalUser.get();
        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new DomainException("Username or password are incorrect.");
        }
        return user;
    }

    @Transactional
    public User register(RegisterRequest registerRequest) {
        Optional<User> optionalUser=userRepository.findByUsername(registerRequest.getUsername());
        if(optionalUser.isPresent()) {
            throw new DomainException("Username [%s] already exists.".formatted(registerRequest.getUsername()));
        }

        User user=userRepository.save(initializeUser(registerRequest));

        subscriptionService.createDefaultSubscription(user);
        walletService.createNewWallet(user);

        log.info("Successfully create new user account for username [%s] and id [%s].".formatted(user.getUsername(), user.getId()));
        return user;
    }

    private User initializeUser(RegisterRequest registerRequest) {
        return User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .country(registerRequest.getCountry())
                .role(userProperty.getDefaultRole())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .isActive(userProperty.isDefaultAccountState())
        .build();
    }
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public User getById(UUID id) {
        return userRepository.findById(id).orElseThrow(()  -> new DomainException("User with id [%s] does not exist.".formatted(id)));
    }
}
