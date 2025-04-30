package com.store_app.controllers;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.store_app.models.Product;
import com.store_app.models.ProductDTO;
import com.store_app.service.ProductService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
public class ProductsController {

	@Autowired
	private ProductService productService;

	@GetMapping("")
	public String showAllProducts(Model model) {
		List<Product> products = productService.getAllProducts();
		model.addAttribute("products", products);
		return "products/index";
	}

	@GetMapping("/{id}")
	public String getProductById(@PathVariable("id") Integer id, Model model) {

		Optional<Product> product = productService.productById(id);

		if (product.isPresent()) {
			model.addAttribute("product", product.get()); // Here we add the product to the model
			return "products/ProductDetails";
		} else {
			return "products/Error";
		}
	}

	@GetMapping("/create")
	public String createPage(Model model) {
		// ProductDTO productDto = new ProductDTO();
		model.addAttribute("productDto", new ProductDTO());
		return "products/CreateProduct";
	}

	@PostMapping("/create")
	public String saveProduct(@Valid @ModelAttribute("productDto") ProductDTO productDto, BindingResult result) {

		// If image file is empty, add an error
		if (productDto.getImageFile().isEmpty()) {
			result.addError(new FieldError("productDto", "imageFile", "The image file is required"));
		}

		// Check if there are validation errors
		if (result.hasErrors()) {
			return "products/CreateProduct"; // Return to the same page with errors
		}

		try {
			productService.saveProduct(productDto);
		} catch (IOException e) {
			System.out.println("file upload error: " + e.getMessage());
			result.rejectValue("imagefile", "uploadError", "Failed to upload image.");
			return "products/CreateProduct";
		}

		// Redirect to the product list after successful save
		return "redirect:/products";
	}

	@GetMapping("/edit")
	public String editPage(@RequestParam Integer id, Model model) {

		try {
			Product product = productService.productById(id)
		            .orElseThrow(() -> new NoSuchElementException("Product not found"));
			ProductDTO productDto = productService.editProductById(id);
			
			model.addAttribute("product", product);
			model.addAttribute("productDto", productDto);
			
		} catch (NoSuchElementException e) {
			System.out.println("Product is not found: " + e.getMessage());
			return "redirect:/products";
		}
		return "products/EditProduct";
	}

	@PostMapping("/edit")
	public String updateProduct(@RequestParam Integer id, @Valid @ModelAttribute ProductDTO productDto,
			BindingResult result, Model model) {

		// Check if there are validation errors
		if (result.hasErrors()) {
			return "products/EditProduct";
		}

		try {
			productService.updateProduct(id, productDto, result);
		} catch (Exception e) {
			System.out.println("Error updating product: " + e.getMessage());
		}

		// Redirect to the products list page after successful update
		return "redirect:/products";
	}

	@GetMapping("/delete")
	public String deleteProduct(@RequestParam Integer id) {

			try {
			// delete the product
			productService.deleteProduct(id);
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
		return "redirect:/products";
	}
}
