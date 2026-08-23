package com.soc.productservice;

import com.soc.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class ProductServiceApplicationTests {

	@MockBean
	private ProductRepository productRepository;

	@Test
	void contextLoads() {
	}

}
