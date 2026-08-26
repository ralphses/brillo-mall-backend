package com.clickstechnology.Brillo.Mall.application.api.controllers;

import com.clickstechnology.Brillo.Mall.application.dto.business.BusinessDto;
import com.clickstechnology.Brillo.Mall.application.dto.business.SharedConversationAttributionDto;
import com.clickstechnology.Brillo.Mall.application.dto.StorefrontData;
import com.clickstechnology.Brillo.Mall.application.dto.response.DashboardData;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.OnboardBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.request.business.UpdateBusinessRequest;
import com.clickstechnology.Brillo.Mall.application.dto.response.PaginatedResponse;
import com.clickstechnology.Brillo.Mall.application.dto.response.business.OnboardBusinessResponse;
import com.clickstechnology.Brillo.Mall.application.enums.BusinessCategory;
import com.clickstechnology.Brillo.Mall.application.exception.BusinessException;
import com.clickstechnology.Brillo.Mall.application.api.contracts.DashboardService;
import com.clickstechnology.Brillo.Mall.application.features.business.OnboardUserBusiness;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class BusinessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OnboardUserBusiness onboardUserBusiness;

    @MockitoBean
    private DashboardService dashboardService;

    private BusinessDto businessDto;

    @BeforeEach
    void setUp() {
        businessDto = BusinessDto.builder()
                .id("biz-123")
                .name("Test Business")
                .slug("test-business")
                .category(BusinessCategory.PRODUCTS)
                .storefrontLink("https://test.brillo.example/api/v1/public/businesses/slug/test-business")
                .sharedWhatsappLink("https://wa.me/2348039999999?text=Hi")
                .whatsappEntryMode("SHARED")
                .dedicatedNumberReady(false)
                .sharedWhatsappManagedByBrillo(true)
                .sharedConversationAttribution(SharedConversationAttributionDto.builder()
                        .entryAttributedCustomersCount(3L)
                        .activeSharedConversationsCount(2L)
                        .build())
                .build();
    }

    @Test
    void onboard_ShouldReturn200_WhenRequestIsValid() throws Exception {
        OnboardBusinessRequest request = new OnboardBusinessRequest("Test Business", "PRODUCTS");
        OnboardBusinessResponse response = OnboardBusinessResponse.builder()
                .message("Business onboarded successfully")
                .storefrontLink("https://test.brillo.example/api/v1/public/businesses/slug/test-business")
                .sharedWhatsappLink("https://wa.me/2348039999999?text=Hi")
                .build();

        when(onboardUserBusiness.execute(any(OnboardBusinessRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/businesses/onboard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("Business onboarded successfully"))
                .andExpect(jsonPath("$.data.storefrontLink").value("https://test.brillo.example/api/v1/public/businesses/slug/test-business"))
                .andExpect(jsonPath("$.data.sharedWhatsappLink").value("https://wa.me/2348039999999?text=Hi"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void uploadLogo_ShouldReturn200_WhenFileIsUploaded() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "logoFile",
                "logo.png",
                MediaType.IMAGE_PNG_VALUE,
                "test image content".getBytes()
        );
        OnboardBusinessResponse response = OnboardBusinessResponse.builder()
                .message("Logo uploaded successfully")
                .storefrontLink("https://test.brillo.example/api/v1/public/businesses/slug/test-business")
                .sharedWhatsappLink("https://wa.me/2348039999999?text=Hi")
                .build();

        when(onboardUserBusiness.uploadLogo(any(), eq("biz-123"), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/v1/businesses/{businessId}/upload-logo", "biz-123")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("Logo uploaded successfully"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void updateBusiness_ShouldReturn200_WhenRequestIsValid() throws Exception {
        UpdateBusinessRequest.BusinessLocation location = new UpdateBusinessRequest.BusinessLocation("123 Main St", "Lagos", "Lagos");
        UpdateBusinessRequest request = new UpdateBusinessRequest("New Name", "New Desc", location, "test@test.com", "1234567890");
        OnboardBusinessResponse response = OnboardBusinessResponse.builder()
                .message("Business updated successfully")
                .storefrontLink("https://test.brillo.example/api/v1/public/businesses/slug/test-business")
                .sharedWhatsappLink("https://wa.me/2348039999999?text=Hi")
                .build();

        when(onboardUserBusiness.updateBusiness(eq("biz-123"), any(UpdateBusinessRequest.class), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/businesses/{businessId}", "biz-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("Business updated successfully"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void getBusinesses_ShouldReturn200_WithPaginatedResponse() throws Exception {
        PaginatedResponse<BusinessDto> paginatedResponse = PaginatedResponse.<BusinessDto>builder()
                .page(1)
                .perPage(20)
                .total(1)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .items(List.of(businessDto))
                .build();

        when(onboardUserBusiness.getBusinesses(eq(1), eq(20), any())).thenReturn(paginatedResponse);

        mockMvc.perform(get("/api/v1/businesses")
                        .param("page", "1")
                        .param("pageSize", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value("biz-123"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void getBusinessById_ShouldReturn200_WhenBusinessExists() throws Exception {
        when(onboardUserBusiness.getBusinessById("biz-123")).thenReturn(businessDto);

        mockMvc.perform(get("/api/v1/businesses/{businessId}", "biz-123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("biz-123"))
                .andExpect(jsonPath("$.data.name").value("Test Business"))
                .andExpect(jsonPath("$.data.storefrontLink").value("https://test.brillo.example/api/v1/public/businesses/slug/test-business"))
                .andExpect(jsonPath("$.data.whatsappEntryMode").value("SHARED"))
                .andExpect(jsonPath("$.data.sharedWhatsappManagedByBrillo").value(true))
                .andExpect(jsonPath("$.data.sharedConversationAttribution.entryAttributedCustomersCount").value(3))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void getBusinessBySlug_ShouldReturn200_WhenBusinessExists() throws Exception {
        when(onboardUserBusiness.getBusinessBySlug("test-business")).thenReturn(businessDto);

        mockMvc.perform(get("/api/v1/businesses/slug/{businessSlug}", "test-business")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slug").value("test-business"))
                .andExpect(jsonPath("$.data.name").value("Test Business"))
                .andExpect(jsonPath("$.data.sharedWhatsappLink").value("https://wa.me/2348039999999?text=Hi"))
                .andExpect(jsonPath("$.data.dedicatedNumberReady").value(false))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void activateStorefront_ShouldReturn200_WhenBusinessIsActivated() throws Exception {
        OnboardBusinessResponse response = OnboardBusinessResponse.builder()
                .message("Storefront has been activated.")
                .storefrontLink("https://test.brillo.example/api/v1/public/businesses/slug/test-business")
                .sharedWhatsappLink("https://wa.me/2348039999999?text=Hi")
                .build();
        when(onboardUserBusiness.activateStorefront(eq("biz-123"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/businesses/{businessId}/activate-storefront", "biz-123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("Storefront has been activated."))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void getDashboard_ShouldReturn200_WithDashboardData() throws Exception {
        DashboardData dashboardData = DashboardData.builder()
                .currentStorefrontData(new StorefrontData(
                        BusinessCategory.PRODUCTS,
                        "biz-123",
                        "owner@test.com",
                        "Test Store",
                        "1234567890",
                        "123 Main Street",
                        "2348039999999",
                        com.clickstechnology.Brillo.Mall.application.enums.WhatsappType.SHARED,
                        true,
                        true,
                        "Test description",
                        "logo.png",
                        "https://test.brillo.example/api/v1/public/businesses/slug/test-business",
                        "https://wa.me/2348039999999?text=Hi",
                        "SHARED",
                        false,
                        true,
                        SharedConversationAttributionDto.builder()
                                .entryAttributedCustomersCount(5L)
                                .activeSharedConversationsCount(3L)
                                .build(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of()
                ))
                .build();
        when(dashboardService.getDashboardData(any())).thenReturn(dashboardData);

        mockMvc.perform(get("/api/v1/businesses/dashboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStorefrontData.whatsappEntryMode").value("SHARED"))
                .andExpect(jsonPath("$.data.currentStorefrontData.sharedWhatsappManagedByBrillo").value(true))
                .andExpect(jsonPath("$.data.currentStorefrontData.sharedConversationAttribution.activeSharedConversationsCount").value(3))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void onboard_ShouldReturn400_WhenRequestBodyIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/businesses/onboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void uploadLogo_ShouldReturn400_WhenFileIsMissing() throws Exception {
        mockMvc.perform(multipart("/api/v1/businesses/{businessId}/upload-logo", "biz-123")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBusinessById_ShouldReturn400_WhenBusinessNotFound() throws Exception {
        when(onboardUserBusiness.getBusinessById("invalid-id"))
                .thenThrow(new BusinessException("Business not found"));

        mockMvc.perform(get("/api/v1/businesses/{businessId}", "invalid-id")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Business not found"));
    }
}
