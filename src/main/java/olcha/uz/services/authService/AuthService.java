package olcha.uz.services.authService;

import lombok.RequiredArgsConstructor;
import olcha.uz.configs.security.UserDetails;
import olcha.uz.domains.auth.AuthUser;
import olcha.uz.dto.auth.UserCreateDto;
import olcha.uz.dto.auth.UserLoginDto;
import olcha.uz.exceptions.MethodNotAllowedException;
import olcha.uz.exceptions.NotFoundException;
import olcha.uz.exceptions.UnAuthorizedException;
import olcha.uz.repository.authRepository.AuthRepository;
import olcha.uz.util.Utils;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {
    private final AuthRepository authRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AuthUser authUser = authRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );
        return new UserDetails(authUser);
    }

    public void login(UserLoginDto userLoginDto) {
        AuthUser user = authRepository.findByUsername(userLoginDto.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found by username %s".formatted(userLoginDto.getUsername())));

        if (!Utils.matchPassword(userLoginDto.getPassword(), user.getPassword())) {
            throw new UnAuthorizedException("Password do not match");
        }
    }

    public void create(UserCreateDto userCreateDto) {
        if (authRepository.findByUsername(userCreateDto.getUsername()).isPresent()) {
            throw new MethodNotAllowedException("Username already taken %s".formatted(userCreateDto.getUsername()));
        }

        if (!userCreateDto.getPassword().equals(userCreateDto.getConfirmPassword()))
            throw new MethodNotAllowedException("Password do not match");


        AuthUser userBuild = AuthUser.builder()
                .fullName(userCreateDto.getFullName())
                .username(userCreateDto.getUsername())
                .password(Utils.encode(userCreateDto.getPassword()))
                .active(true)
                .build();

        authRepository.save(userBuild);
    }
}
