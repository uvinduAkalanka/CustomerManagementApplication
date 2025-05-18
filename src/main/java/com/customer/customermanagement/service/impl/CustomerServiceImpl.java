package com.customer.customermanagement.service.impl;


import com.customer.customermanagement.dto.AddressDTO;
import com.customer.customermanagement.dto.CustomerDTO;
import com.customer.customermanagement.exception.DuplicateResourceException;
import com.customer.customermanagement.exception.ResourceNotFoundException;
import com.customer.customermanagement.model.Address;
import com.customer.customermanagement.model.City;
import com.customer.customermanagement.model.Country;
import com.customer.customermanagement.model.Customer;
import com.customer.customermanagement.repository.AddressRepository;
import com.customer.customermanagement.repository.CityRepository;
import com.customer.customermanagement.repository.CountryRepository;
import com.customer.customermanagement.repository.CustomerRepository;
import com.customer.customermanagement.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;
    private final AddressRepository addressRepository;

    private static final int BATCH_SIZE = 1000;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @Transactional
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        // Check if customer with same NIC already exists
        if (customerRepository.existsByNicNumber(customerDTO.getNicNumber())) {
            throw new DuplicateResourceException("Customer with NIC " + customerDTO.getNicNumber() + " already exists");
        }

        Customer customer = mapToEntity(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);

        return mapToDTO(savedCustomer);
    }

    @Override
    @Transactional
    public CustomerDTO updateCustomer(Long id, CustomerDTO customerDTO) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        // Check if updated NIC conflicts with another customer
        if (!customer.getNicNumber().equals(customerDTO.getNicNumber()) &&
                customerRepository.existsByNicNumber(customerDTO.getNicNumber())) {
            throw new DuplicateResourceException("Customer with NIC " + customerDTO.getNicNumber() + " already exists");
        }

        // Update customer fields
        customer.setName(customerDTO.getName());
        customer.setDateOfBirth(customerDTO.getDateOfBirth());
        customer.setNicNumber(customerDTO.getNicNumber());

        // Update mobile numbers
        if (customerDTO.getMobileNumbers() != null) {
            customer.getMobileNumbers().clear();
            customer.getMobileNumbers().addAll(customerDTO.getMobileNumbers());
        }

        // Update family members
        if (customerDTO.getFamilyMemberIds() != null) {
            customer.getFamilyMembers().clear();
            customerDTO.getFamilyMemberIds().forEach(familyMemberId -> {
                customerRepository.findById(familyMemberId).ifPresent(familyMember -> {
                    customer.getFamilyMembers().add(familyMember);
                });
            });
        }

        // Update addresses
        if (customerDTO.getAddresses() != null) {
            customer.getAddresses().clear();
            customerDTO.getAddresses().forEach(addressDTO -> {
                Address address = new Address();
                address.setAddressLine1(addressDTO.getAddressLine1());
                address.setAddressLine2(addressDTO.getAddressLine2());

                // Get or create city and country
                City city = getOrCreateCity(addressDTO.getCityName(), addressDTO.getCountryName());
                address.setCity(city);
                address.setCustomer(customer);

                customer.getAddresses().add(address);
            });
        }

        Customer updatedCustomer = customerRepository.save(customer);
        return mapToDTO(updatedCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        return mapToDTO(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerDTO> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        // Remove this customer from family relationships
        customer.getFamilyMembers().forEach(familyMember -> {
            familyMember.getFamilyMembers().remove(customer);
        });

        customerRepository.delete(customer);
    }

    @Override
    @Async
    @Transactional
    public CompletableFuture<List<String>> processBulkCustomerCreation(MultipartFile file) {
        log.info("Starting bulk customer creation process");
        List<String> errors = new ArrayList<>();
        int totalProcessed = 0;
        int successCount = 0;

        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);

            List<Customer> batchCustomers = new ArrayList<>(BATCH_SIZE);

            // Skip header row
            Iterator<Row> rowIterator = sheet.rowIterator();
            if (rowIterator.hasNext()) {
                rowIterator.next(); // Skip header row
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                totalProcessed++;

                try {
                    Customer customer = extractCustomerFromRow(row);

                    if (customer != null) {
                        batchCustomers.add(customer);
                        successCount++;

                        // Process in batches for better memory management
                        if (batchCustomers.size() >= BATCH_SIZE) {
                            customerRepository.saveAll(batchCustomers);
                            log.info("Processed batch of {} customers", batchCustomers.size());
                            batchCustomers.clear();
                        }
                    }
                } catch (Exception e) {
                    String errorMsg = "Error processing row " + row.getRowNum() + ": " + e.getMessage();
                    log.error(errorMsg);
                    errors.add(errorMsg);
                }

                // Log progress periodically
                if (totalProcessed % 10000 == 0) {
                    log.info("Processed {} customer records so far", totalProcessed);
                }
            }

            // Save any remaining records in the last batch
            if (!batchCustomers.isEmpty()) {
                customerRepository.saveAll(batchCustomers);
                log.info("Processed final batch of {} customers", batchCustomers.size());
            }

            workbook.close();
        } catch (IOException e) {
            String errorMsg = "Error processing file: " + e.getMessage();
            log.error(errorMsg);
            errors.add(errorMsg);
        }

        log.info("Completed bulk processing. Total: {}, Success: {}, Failed: {}",
                totalProcessed, successCount, errors.size());

        return CompletableFuture.completedFuture(errors);
    }

    private Customer extractCustomerFromRow(Row row) {
        // Get cell values
        String name = getCellStringValue(row.getCell(0));
        String dobString = getCellStringValue(row.getCell(1));
        String nicNumber = getCellStringValue(row.getCell(2));

        // Validate mandatory fields
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is mandatory");
        }

        if (nicNumber == null || nicNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("NIC number is mandatory");
        }

        // Check if customer with same NIC already exists
        if (customerRepository.existsByNicNumber(nicNumber)) {
            throw new DuplicateResourceException("Customer with NIC " + nicNumber + " already exists");
        }

        // Parse date of birth
        LocalDate dateOfBirth;
        try {
            dateOfBirth = LocalDate.parse(dobString, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format for date of birth. Use YYYY-MM-DD format");
        }

        // Create customer entity
        return Customer.builder()
                .name(name)
                .dateOfBirth(dateOfBirth)
                .nicNumber(nicNumber)
                .build();
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return DATE_FORMATTER.format(cell.getLocalDateTimeCellValue().toLocalDate());
                }
                return String.valueOf((int) cell.getNumericCellValue());
            default:
                return null;
        }
    }

    private Customer mapToEntity(CustomerDTO dto) {
        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setDateOfBirth(dto.getDateOfBirth());
        customer.setNicNumber(dto.getNicNumber());

        if (dto.getMobileNumbers() != null) {
            customer.setMobileNumbers(new HashSet<>(dto.getMobileNumbers()));
        }

        // Add family members if provided
        if (dto.getFamilyMemberIds() != null && !dto.getFamilyMemberIds().isEmpty()) {
            Set<Customer> familyMembers = dto.getFamilyMemberIds().stream()
                    .map(id -> customerRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Family member not found with id: " + id)))
                    .collect(Collectors.toSet());
            customer.setFamilyMembers(familyMembers);
        }

        // Add addresses if provided
        if (dto.getAddresses() != null && !dto.getAddresses().isEmpty()) {
            List<Address> addresses = dto.getAddresses().stream()
                    .map(addressDTO -> {
                        Address address = new Address();
                        address.setAddressLine1(addressDTO.getAddressLine1());
                        address.setAddressLine2(addressDTO.getAddressLine2());

                        // Get or create city and country
                        City city = getOrCreateCity(addressDTO.getCityName(), addressDTO.getCountryName());
                        address.setCity(city);
                        address.setCustomer(customer);

                        return address;
                    })
                    .collect(Collectors.toList());

            customer.setAddresses(addresses);
        }

        return customer;
    }

    private City getOrCreateCity(String cityName, String countryName) {
        if (cityName == null || countryName == null) {
            throw new IllegalArgumentException("City and country names must be provided");
        }

        // Find or create country
        Country country = countryRepository.findByName(countryName)
                .orElseGet(() -> {
                    Country newCountry = new Country();
                    newCountry.setName(countryName);
                    return countryRepository.save(newCountry);
                });

        // Find or create city
        return cityRepository.findByNameAndCountryId(cityName, country.getId())
                .orElseGet(() -> {
                    City newCity = new City();
                    newCity.setName(cityName);
                    newCity.setCountry(country);
                    return cityRepository.save(newCity);
                });
    }

    private CustomerDTO mapToDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setDateOfBirth(customer.getDateOfBirth());
        dto.setNicNumber(customer.getNicNumber());
        dto.setMobileNumbers(customer.getMobileNumbers());

        // Map family members to IDs
        if (customer.getFamilyMembers() != null && !customer.getFamilyMembers().isEmpty()) {
            dto.setFamilyMemberIds(customer.getFamilyMembers().stream()
                    .map(Customer::getId)
                    .collect(Collectors.toSet()));
        }

        // Map addresses
        if (customer.getAddresses() != null && !customer.getAddresses().isEmpty()) {
            dto.setAddresses(customer.getAddresses().stream()
                    .map(this::mapAddressToDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private AddressDTO mapAddressToDTO(Address address) {
        AddressDTO dto = new AddressDTO();
        dto.setId(address.getId());
        dto.setAddressLine1(address.getAddressLine1());
        dto.setAddressLine2(address.getAddressLine2());

        if (address.getCity() != null) {
            dto.setCityId(address.getCity().getId());
            dto.setCityName(address.getCity().getName());

            if (address.getCity().getCountry() != null) {
                dto.setCountryId(address.getCity().getCountry().getId());
                dto.setCountryName(address.getCity().getCountry().getName());
            }
        }

        return dto;
    }
}
