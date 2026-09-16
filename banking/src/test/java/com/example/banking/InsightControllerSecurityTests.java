package com.example.banking;

import com.example.banking.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InsightControllerSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void authenticatedUserCanReadMockMoneyHealth() throws Exception {
        mockMvc.perform(get("/api/insights/my-financial-health").with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(50))
                .andExpect(jsonPath("$.status").value("NEEDS_ATTENTION"));
    }

    @Test
    void authenticatedAdminCanReadMockMoneyHealth() throws Exception {
        mockMvc.perform(get("/api/insights/my-financial-health").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void validJwtAuthenticatesUserForMockMoneyHealth() throws Exception {
        String accessToken = jwtTokenProvider.generateAccessToken("user@example.com", "USER", 1L);

        mockMvc.perform(get("/api/insights/my-financial-health")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void invalidJwtIsRejected() throws Exception {
        mockMvc.perform(get("/api/insights/my-financial-health")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/insights/my-financial-health"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedUserWithoutAllowedRoleIsRejected() throws Exception {
        mockMvc.perform(get("/api/insights/my-financial-health").with(user("other").roles("OTHER")))
                .andExpect(status().isForbidden());
    }
}
