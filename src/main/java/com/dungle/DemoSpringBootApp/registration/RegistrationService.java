package com.dungle.DemoSpringBootApp.registration;

import com.dungle.DemoSpringBootApp.entity.AppUser;
import com.dungle.DemoSpringBootApp.entity.AppUserRepository;
import com.dungle.DemoSpringBootApp.entity.AppUserRole;
import com.dungle.DemoSpringBootApp.entity.AppUserService;
import com.dungle.DemoSpringBootApp.registration.token.ConfirmationToken;
import com.dungle.DemoSpringBootApp.registration.token.ConfirmationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class RegistrationService {
    private final EmailValidator emailValidator;
    private final AppUserService appUserService;
    private final ConfirmationTokenService confirmationTokenService;
    public String register(RegistrationRequest registrationRequest){
        boolean isValidEmail = emailValidator.test(registrationRequest.getEmail());
        if(!isValidEmail){
            throw new IllegalStateException("email not valid");
        }
        return appUserService.signUpUser(
                new AppUser(
                        registrationRequest.getFirstName(),
                        registrationRequest.getLastName(),
                        registrationRequest.getEmail(),
                        registrationRequest.getPassword(),
                        AppUserRole.USER
                )
        );
    }

    public String confirmToken(String token){
        ConfirmationToken confirmationToken = confirmationTokenService.getToken(token).orElseThrow(()->new IllegalStateException("token not found"));
        if(confirmationToken.getConfirmedAt() != null){
            throw  new IllegalStateException("email already confirmed");
        }
        LocalDateTime expiredAt = confirmationToken.getExpiresAt();
        if(expiredAt.isBefore(LocalDateTime.now())){
            throw new IllegalStateException("token expired");
        }
        confirmationTokenService.setConfirmedAt(token);
        appUserService.enableAppUser(confirmationToken.getAppUser().getEmail());
        return "confirmed";
    }
}
