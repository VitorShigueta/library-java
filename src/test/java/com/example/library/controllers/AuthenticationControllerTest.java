package com.example.library.controllers;

import com.example.library.dtos.AuthenticationDTO;
import com.example.library.dtos.RegisterDTO;
import com.example.library.entities.User;
import com.example.library.enums.UserRole;
import com.example.library.repositories.UserRepository;
import com.example.library.services.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private TokenService tokenService;

    private User defaultUser;
    private RegisterDTO defaultRegisterDTO;
    private AuthenticationDTO defaultAuthDTO;

    @BeforeEach
    public void setup() {
        defaultUser = createDefaultUser("leitor@email.com", UserRole.USER);
        defaultRegisterDTO = new RegisterDTO("leitor@emial", "senha123");
        defaultAuthDTO = new AuthenticationDTO("leitor@emial", "senha123");
    }

    private User createDefaultUser(String email, UserRole role) {
        return new User(email, "senhaHashCriptografada", Set.of(role));
    }

    @Nested
    @DisplayName("POST /auth/register")
    class RegisterTests {

        @Test
        @DisplayName("It should successfully register a new user and return 200")
        void shouldRegisterSucessfully() throws Exception {
            when(userRepository.findByEmail(defaultRegisterDTO.email())).thenReturn(Optional.empty());

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(defaultRegisterDTO)))
                    .andExpect(status().isOk());

            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Should return 400 if the email is already registered.")
        void shouldReturnBadRequestWhenEmailExists() throws Exception {
            when(userRepository.findByEmail(defaultRegisterDTO.email())).thenReturn(Optional.of(defaultUser));

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(defaultRegisterDTO)))
                    .andExpect(status().isBadRequest());

            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("POST /auth/login")
    class LoginTests {

        @Test
        @DisplayName("It should authenticate successfully and return a token.")
        void shouldLoginSucessfully() throws Exception {
            Authentication authMock = mock(Authentication.class);
            when(authMock.getPrincipal()).thenReturn(defaultUser);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authMock);
            when(tokenService.gerateToken(defaultUser)).thenReturn("token_valido_123");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(defaultAuthDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("token_valido_123"));
        }

        @Test
        @DisplayName("Should fail and not generate a token when credentials are invalid.")
        void shouldFailWhenCredentialAreInvalid() throws Exception {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new BadCredentialsException("Username or password is invalid"));

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(defaultAuthDTO)))
                    .andExpect(status().isForbidden());

            verify(tokenService, never()).gerateToken(any());
        }
    }
}
