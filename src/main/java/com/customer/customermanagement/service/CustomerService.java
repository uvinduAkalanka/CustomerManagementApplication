package com.customer.customermanagement.service;


import com.customer.customermanagement.dto.CustomerDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CustomerService {

    CustomerDTO createCustomer(CustomerDTO customerDTO);

    CustomerDTO updateCustomer(Long id, CustomerDTO customerDTO);

    CustomerDTO getCustomerById(Long id);

    Page<CustomerDTO> getAllCustomers(Pageable pageable);

    void deleteCustomer(Long id);

    CompletableFuture<List<String>> processBulkCustomerCreation(MultipartFile file);
}
