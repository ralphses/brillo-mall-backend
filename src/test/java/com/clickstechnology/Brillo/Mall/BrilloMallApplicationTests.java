package com.clickstechnology.Brillo.Mall;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.clickstechnology.Brillo.Mall.application.features.orders.PlaceOrder;

@SpringBootTest
class BrilloMallApplicationTests {

	@MockitoBean
	private PlaceOrder placeOrder;

	@Test
	void contextLoads() {
	}

}
