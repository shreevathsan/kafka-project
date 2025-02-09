package com.appsdeveloperblog.ws.products.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.appsdeveloperblog.ws.products.model.CreateProductRestModel;
import com.appsdeveloperblog.ws.products.service.ProductService;

@RestController
@RequestMapping("/products")
public class ProductController {

	@Autowired
	private ProductService productService;

	@PostMapping("")
	public ResponseEntity<String> createProduct(@RequestBody CreateProductRestModel createProductRestModel)
			throws Exception {
		String response = productService.createProduct(createProductRestModel);
		return ResponseEntity.ok(response);

	}

}
