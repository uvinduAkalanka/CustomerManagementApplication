package com.customer.customermanagement.repository;


import com.customer.customermanagement.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    Optional<City> findByNameAndCountryId(String name, Long countryId);
}

