package com.zycus.StockPulse;

import com.zycus.StockPulse.product.Product;
import com.zycus.StockPulse.product.ProductRepository;
import com.zycus.StockPulse.product.enums.ProductCategory;
import com.zycus.StockPulse.suggestion.PricingSuggestionRepository;
import com.zycus.StockPulse.suggestion.ReorderSuggestionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class StockPulseApplicationTests {

	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private PricingSuggestionRepository pricingSuggestionRepository;
	
	@Autowired
	private ReorderSuggestionRepository reorderSuggestionRepository;

	@Test
	void contextLoads() {
		// This test verifies that the Spring context loads successfully
	}

	@Test
	void testProductCreation() {
		// Create a new product
		Product product = new Product();
		product.setSku("TEST001");
		product.setName("Test Product");
		product.setCategory(ProductCategory.APPAREL);
		product.setStockLevel(10);
		product.setReorderThreshold(5);
		product.setCurrentPrice(new BigDecimal("29.99"));
		product.setCostPrice(new BigDecimal("15.00"));
		
		// Save the product
		Product savedProduct = productRepository.save(product);
		
		// Verify the product was saved
		assertThat(savedProduct.getId()).isNotNull();
		assertThat(savedProduct.getSku()).isEqualTo("TEST001");
		assertThat(savedProduct.getName()).isEqualTo("Test Product");
		assertThat(savedProduct.getCategory()).isEqualTo(ProductCategory.APPAREL);
		assertThat(savedProduct.getStockLevel()).isEqualTo(10);
		assertThat(savedProduct.getReorderThreshold()).isEqualTo(5);
		assertThat(savedProduct.getCurrentPrice()).isEqualByComparingTo(new BigDecimal("29.99"));
		assertThat(savedProduct.getCostPrice()).isEqualByComparingTo(new BigDecimal("15.00"));
	}

	@Test
	@Sql("/test-data.sql")
	void testRepositoriesAreEmptyInitially() {
		// Verify that repositories are initially empty
		List<Product> products = productRepository.findAll();
		assertThat(products).isEmpty();
		
		assertThat(pricingSuggestionRepository.count()).isEqualTo(0);
		assertThat(reorderSuggestionRepository.count()).isEqualTo(0);
	}
}
