package com.example.sponsorships.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.sponsorships.dto.DashboardView;
import com.example.sponsorships.enums.ContractStatus;
import com.example.sponsorships.service.DashboardService;
import java.util.EnumMap;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DashboardService dashboardService;

    @TestConfiguration
    static class MockDashboardService {

        @Bean
        DashboardService dashboardService() {
            return mock(DashboardService.class);
        }
    }

    @Test
    void rootRedirectsToDashboard() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void dashboardRendersTheViewWithItsModel() throws Exception {
        DashboardView view = new DashboardView(2L, new EnumMap<>(ContractStatus.class), List.of());
        when(dashboardService.load()).thenReturn(view);

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attribute("dashboard", view));
    }
}
