package com.customer.customermanagement;


import com.customer.customermanagement.dto.CustomerDTO;
import com.customer.customermanagement.exception.DuplicateResourceException;
import com.customer.customermanagement.exception.ResourceNotFoundException;
import com.customer.customermanagement.model.Customer;
import com.customer.customermanagement.repository.AddressRepository;
import com.customer.customermanagement.repository.CityRepository;
import com.customer.customermanagement.repository.CountryRepository;
import com.customer.customermanagement.repository.CustomerRepository;
import com.customer.customermanagement.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer;
    private CustomerDTO customerDTO;

    @BeforeEach
    public void setup() {
        // Initialize customer entity
        customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");
        customer.setDateOfBirth(LocalDate.of(1990, 5, 15));
        customer.setNicNumber("123456789X");
        customer.setMobileNumbers(new HashSet<>(Arrays.asList("1234567890", "0987654321")));

        // Initialize customer DTO
        customerDTO = new CustomerDTO();
        customerDTO.setId(1L);
        customerDTO.setName("John Doe");
        customerDTO.setDateOfBirth(LocalDate.of(1990, 5, 15));
        customerDTO.setNicNumber("123456789X");
        customerDTO.setMobileNumbers(new HashSet<>(Arrays.asList("1234567890", "0987654321")));
    }

    @Test
    public void testCreateCustomer_Success() {
        // Given
        when(customerRepository.existsByNicNumber(any())).thenReturn(false);
        when(customerRepository.save(any())).thenReturn(customer);

        // When
        CustomerDTO result = customerService.createCustomer(customerDTO);

        // Then
        assertNotNull(result);
        assertEquals(customerDTO.getName(), result.getName());
        assertEquals(customerDTO.getNicNumber(), result.getNicNumber());
        verify(customerRepository, times(1)).save(any());
    }

    @Test
    public void testCreateCustomer_DuplicateNic() {
        // Given
        when(customerRepository.existsByNicNumber(any())).thenReturn(true);

        // When/Then
        assertThrows(DuplicateResourceException.class, () -> {
            customerService.createCustomer(customerDTO);
        });

        verify(customerRepository, never()).save(any());
    }

    @Test
    public void testGetCustomerById_Success() {
        // Given
        when(customerRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(customer));

        // When
        CustomerDTO result = customerService.getCustomerById(1L);

        // Then
        assertNotNull(result);
        assertEquals(customerDTO.getId(), result.getId());
        assertEquals(customerDTO.getName(), result.getName());
    }

    @Test
    public void testGetCustomerById_NotFound() {
        // Given
        when(customerRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> {
            customerService.getCustomerById(99L);
        });
    }

    @Test
    public void testGetAllCustomers() {
        // Given
        List<Customer> customers = Collections.singletonList(customer);
        Page<Customer> customerPage = new PageImpl<>(customers);
        Pageable pageable = PageRequest.of(0, 10);

        when(customerRepository.findAll(pageable)).thenReturn(customerPage);

        // When
        Page<CustomerDTO> result = customerService.getAllCustomers(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(customerDTO.getName(), result.getContent().get(0).getName());
    }

    @Test
    public void testUpdateCustomer_Success() {
        // Given
        CustomerDTO updateDTO = new CustomerDTO();
        updateDTO.setName("Updated Name");
        updateDTO.setDateOfBirth(LocalDate.of(1990, 5, 15));
        updateDTO.setNicNumber("123456789X");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any())).thenReturn(customer);

        // When
        CustomerDTO result = customerService.updateCustomer(1L, updateDTO);

        // Then
        assertNotNull(result);
        assertEquals(updateDTO.getName(), "Updated Name");
        verify(customerRepository, times(1)).save(any());
    }

    @Test
    public void testUpdateCustomer_NotFound() {
        // Given
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> {
            customerService.updateCustomer(99L, customerDTO);
        });

        verify(customerRepository, never()).save(any());
    }

    @Test
    public void testDeleteCustomer_Success() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        doNothing().when(customerRepository).delete(any());

        // When
        customerService.deleteCustomer(1L);

        // Then
        verify(customerRepository, times(1)).delete(any());
    }

    @Test
    public void testDeleteCustomer_NotFound() {
        // Given
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> {
            customerService.deleteCustomer(99L);
        });

        verify(customerRepository, never()).delete(any());
    }
}