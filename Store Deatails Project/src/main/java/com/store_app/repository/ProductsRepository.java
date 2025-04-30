package com.store_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.store_app.models.Product;

public interface ProductsRepository extends JpaRepository<Product, Integer> {
	
}