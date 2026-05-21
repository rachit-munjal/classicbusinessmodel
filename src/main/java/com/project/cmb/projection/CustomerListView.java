package com.project.cmb.projection;

import org.springframework.data.rest.core.config.Projection;
import com.project.cmb.entity.Customer;

import java.math.BigDecimal;

@Projection(name = "customerList", types = { Customer.class })
public interface CustomerListView {

    Integer getCustomerNumber();
    String getCustomerName();
    String getContactFirstName();
    String getContactLastName();
    String getCity();
    String getCountry();
    BigDecimal getCreditLimit();
}