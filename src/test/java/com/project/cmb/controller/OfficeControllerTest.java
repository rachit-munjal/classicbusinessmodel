package com.project.cmb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.cmb.entity.Office;
import com.project.cmb.projection.EmployeeListView;
import com.project.cmb.projection.OfficeDetailView;
import com.project.cmb.projection.OfficeListView;
import com.project.cmb.repo.EmployeeRepo;
import com.project.cmb.repo.OfficeRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OfficeController.class)
class OfficeControllerTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean OfficeRepo   officeRepo;
    @MockitoBean
    EmployeeRepo employeeRepo;

    // ─── Helpers ──────────────────────────────────────────────────

    private OfficeListView buildOfficeView(String code, String city,
                                           String country, String territory, String phone) {
        return new OfficeListView() {
            public String getOfficeCode()  { return code;      }
            public String getCity()        { return city;      }
            public String getCountry()     { return country;   }
            public String getTerritory()   { return territory; }
            public String getPhone()       { return phone;     }
        };
    }

    private OfficeDetailView buildOfficeDetailView(String code, String city,
                                                   String country, String phone) {
        return new OfficeDetailView() {
            public String getOfficeCode()   { return code;    }
            public String getCity()         { return city;    }
            public String getCountry()      { return country; }
            public String getPhone()        { return phone;   }
            public String getAddressLine1() { return "100 Market Street"; }
            public String getAddressLine2() { return null;    }
            public String getState()        { return null;    }
            public String getPostalCode()   { return "94080"; }
            public String getTerritory()    { return "NA";    }
        };
    }

    private EmployeeListView buildEmployeeView(Integer number,
                                               String firstName, String lastName, String jobTitle) {
        return new EmployeeListView() {
            public Integer     getEmployeeNumber() { return number;    }
            public String      getFirstName()      { return firstName; }
            public String      getLastName()       { return lastName;  }
            public String      getJobTitle()       { return jobTitle;  }
            public OfficeInfo  getOffice()         { return null;      }
            public ManagerInfo getReportsTo()      { return null;      }
        };
    }

    private Office buildOfficeEntity(String code, String city, String country, String phone) {
        Office o = new Office();
        o.setOfficeCode(code);
        o.setCity(city);
        o.setCountry(country);
        o.setPhone(phone);
        o.setAddressLine1("100 Market Street");
        o.setTerritory("NA");
        o.setPostalCode("94080");
        return o;
    }

    // ─── GET /all ─────────────────────────────────────────────────

    @Test
    void getAllOffices_shouldReturn200AndList() throws Exception {
        OfficeListView o1 = buildOfficeView("1", "San Francisco", "USA",   "NA",   "+1 650 219 4782");
        OfficeListView o2 = buildOfficeView("4", "Paris",         "France","EMEA", "+33 14 723 4404");

        when(officeRepo.findAllProjectedBy()).thenReturn(List.of(o1, o2));

        mockMvc.perform(get("/api/v1/offices/all"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].officeCode").value("1"))
                .andExpect(jsonPath("$[0].city").value("San Francisco"))
                .andExpect(jsonPath("$[1].country").value("France"));
    }

    @Test
    void getAllOffices_emptyList_shouldReturn200() throws Exception {
        when(officeRepo.findAllProjectedBy()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/offices/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── GET /{officeCode} ────────────────────────────────────────

    @Test
    void getOffice_found_shouldReturn200AndFields() throws Exception {
        OfficeDetailView view = buildOfficeDetailView("1", "San Francisco", "USA", "+1 650 219 4782");
        when(officeRepo.findOfficeDetailByOfficeCode("1")).thenReturn(Optional.of(view));

        mockMvc.perform(get("/api/v1/offices/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.officeCode").value("1"))
                .andExpect(jsonPath("$.city").value("San Francisco"))
                .andExpect(jsonPath("$.country").value("USA"))
                .andExpect(jsonPath("$.phone").value("+1 650 219 4782"));
    }

    @Test
    void getOffice_notFound_shouldReturn404() throws Exception {
        when(officeRepo.findOfficeDetailByOfficeCode("999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/offices/999"))
                .andExpect(status().isNotFound());
    }

    // ─── GET /stats ───────────────────────────────────────────────

    @Test
    void getStats_shouldReturn200AndFields() throws Exception {
        when(officeRepo.count()).thenReturn(7L);
        when(employeeRepo.count()).thenReturn(23L);

        mockMvc.perform(get("/api/v1/offices/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalOffices").value(7))
                .andExpect(jsonPath("$.totalEmployees").value(23));
    }

    // ─── GET /filter/country ──────────────────────────────────────

    @Test
    void filterByCountry_shouldReturn200AndList() throws Exception {
        OfficeListView o1 = buildOfficeView("1", "San Francisco", "USA",   "NA",   "+1 650 219 4782");
        OfficeListView o2 = buildOfficeView("4", "Paris",         "France","EMEA", "+33 14 723 4404");

        when(officeRepo.findByCountryIn(List.of("USA", "France"))).thenReturn(List.of(o1, o2));

        mockMvc.perform(get("/api/v1/offices/filter/country")
                        .param("countries", "USA")
                        .param("countries", "France"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].officeCode").value("1"))
                .andExpect(jsonPath("$[0].country").value("USA"))
                .andExpect(jsonPath("$[1].country").value("France"));
    }

    @Test
    void filterByCountry_noMatch_shouldReturnEmptyList() throws Exception {
        when(officeRepo.findByCountryIn(List.of("Antarctica"))).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/offices/filter/country")
                        .param("countries", "Antarctica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── GET /{officeCode}/employees ──────────────────────────────

    @Test
    void getEmployees_shouldReturn200AndPage() throws Exception {
        EmployeeListView emp = buildEmployeeView(1002, "Diane", "Murphy", "President");

        when(employeeRepo.findByOffice_OfficeCode(eq("1"), any()))
                .thenReturn(new PageImpl<>(List.of(emp), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/offices/1/employees"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].employeeNumber").value(1002))
                .andExpect(jsonPath("$.content[0].firstName").value("Diane"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getEmployees_invalidOffice_shouldReturnEmptyPage() throws Exception {
        when(employeeRepo.findByOffice_OfficeCode(eq("999"), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/offices/999/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ─── POST /add ────────────────────────────────────────────────

    @Test
    void addOffice_shouldReturn201AndOfficeCode() throws Exception {
        when(officeRepo.existsById("9")).thenReturn(false);
        Office saved = buildOfficeEntity("9", "Mumbai", "India", "+91 22 1234 5678");
        when(officeRepo.save(any(Office.class))).thenReturn(saved);

        Map<String, Object> body = Map.of(
                "officeCode",   "9",
                "city",         "Mumbai",
                "country",      "India",
                "phone",        "+91 22 1234 5678",
                "postalCode",   "400001",
                "territory",    "APAC",
                "addressLine1", "123 Marine Drive"
        );

        mockMvc.perform(post("/api/v1/offices/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.officeCode").value("9"))
                .andExpect(jsonPath("$.message").value("Office created"));
    }

    @Test
    void addOffice_missingOfficeCode_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "city",    "Mumbai",
                "country", "India"
        );

        mockMvc.perform(post("/api/v1/offices/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("officeCode is required"));
    }

    @Test
    void addOffice_duplicate_shouldReturn409() throws Exception {
        when(officeRepo.existsById("1")).thenReturn(true);

        Map<String, Object> body = Map.of(
                "officeCode",   "1",
                "city",         "San Francisco",
                "country",      "USA",
                "phone",        "+1 650 219 4782",
                "postalCode",   "94080",
                "territory",    "NA",
                "addressLine1", "100 Market Street"
        );

        mockMvc.perform(post("/api/v1/offices/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }

    // ─── PUT /{officeCode}/phone ───────────────────────────────────

    @Test
    void updatePhone_shouldReturn204() throws Exception {
        Office office = buildOfficeEntity("1", "San Francisco", "USA", "+1 650 219 4782");
        when(officeRepo.findById("1")).thenReturn(Optional.of(office));
        when(officeRepo.save(any(Office.class))).thenReturn(office);

        mockMvc.perform(put("/api/v1/offices/1/phone")
                        .param("phone", "+1 650 999 9999"))
                .andExpect(status().isNoContent());
    }

    @Test
    void updatePhone_officeNotFound_shouldReturn404() throws Exception {
        when(officeRepo.findById("999")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/offices/999/phone")
                        .param("phone", "+1 999 999 9999"))
                .andExpect(status().isNotFound());
    }
}