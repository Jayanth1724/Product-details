package com.store_app.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.store_app.models.Product;
import com.store_app.models.ProductDTO;
import com.store_app.repository.ProductsRepository;

@Service
public class ProductService {

	private final String uploadFile = "public/images/";

	@Autowired
	private ProductsRepository productsRepo;

	public List<Product> getAllProducts() {
		return productsRepo.findAll();
	}

	public Optional<Product> productById(Integer id) {
		return productsRepo.findById(id);
	}

	public void saveProduct(ProductDTO productDto) throws IOException {

		MultipartFile image = productDto.getImageFile();
		String storageFileName = image.getOriginalFilename();

		Path uploadPath = Paths.get(uploadFile);

		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}

		// Paths.get(uploadFile + storageFileName)
		try (InputStream input = image.getInputStream()) {
			Files.copy(input, uploadPath.resolve(storageFileName), StandardCopyOption.REPLACE_EXISTING);
		}

		// convert DTO to Entity
		Product product = new Product();
		product.setName(productDto.getName());
		product.setBrand(productDto.getBrand());
		product.setCategory(productDto.getCategory());
		product.setPrice(productDto.getPrice());
		product.setDescription(productDto.getDescription());
		product.setImageFileName(storageFileName);

		productsRepo.save(product);
	}

	public ProductDTO editProductById(Integer id) {

		Product product = productsRepo.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Product not found with id: " + id));

		ProductDTO productDto = new ProductDTO();
		
		productDto.setName(product.getName());
		productDto.setBrand(product.getBrand());
		productDto.setCategory(product.getCategory());
		productDto.setPrice(product.getPrice());
		productDto.setDescription(product.getDescription());

		return productDto;
	}

	public void updateProduct(Integer id, ProductDTO productDto, BindingResult result) {

		Product product = productsRepo.findById(id).orElseThrow(() -> new NoSuchElementException("product not found with id: " + id));

		if (!productDto.getImageFile().isEmpty()) {
			MultipartFile newImage = productDto.getImageFile();
			String newImageFileName = newImage.getOriginalFilename();
			
			try {
				
				//delete old image
				Path oldImagePath = Paths.get(uploadFile + product.getImageFileName());
				Files.deleteIfExists(oldImagePath);
				
				// Save new image
				try(InputStream input = newImage.getInputStream()) {
					Files.copy(input, Paths.get(uploadFile + newImageFileName), StandardCopyOption.REPLACE_EXISTING);
				}
				
				product.setImageFileName(newImageFileName);
			} catch (IOException e) {
				result.rejectValue("imageFile", "error.imageFile", "Failed to upload Image.");
				return;
			}
		}
		
		// update fields
		product.setName(productDto.getName());
		product.setBrand(productDto.getBrand());
		product.setCategory(productDto.getCategory());
		product.setPrice(productDto.getPrice());
		product.setDescription(productDto.getDescription());

		productsRepo.save(product);
	}
	
	public void deleteProduct(Integer id) {
		Product product = productsRepo.findById(id).orElseThrow(() -> new NoSuchElementException("product not found with id: " + id));
		
		try {
			Path imagePath = Paths.get(uploadFile, product.getImageFileName());
			Files.deleteIfExists(imagePath);
		} catch (IOException e) {
			System.out.println("failed to delete image: "+ e.getMessage());
		}
		
		productsRepo.delete(product);
	}
}
