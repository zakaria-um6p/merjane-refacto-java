package com.nimbleways.springboilerplate.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

// import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;

// Specify the controller class you want to test
// This indicates to spring boot to only load UsersController into the context
// Which allows a better performance and needs to do less mocks
@SpringBootTest
@AutoConfigureMockMvc
public class MyControllerIntegrationTests {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private NotificationService notificationService;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private ProductRepository productRepository;

	@Test
	void processOrderShouldReturn() throws Exception {
		
		Product usbCable = new Product(null, 15, 30, "NORMAL", "USB Cable", null, null, null);
		Product usbDongle = new Product(null, 10, 0, "NORMAL", "USB Dongle", null, null, null);
		Product butter = new Product(null, 15, 30, "EXPIRABLE", "Butter", LocalDate.now().plusDays(26), null, null);
		Product milk = new Product(null, 90, 6, "EXPIRABLE", "Milk", LocalDate.now().minusDays(2), null, null);
		Product watermelon = new Product(null, 15, 30, "SEASONAL", "Watermelon", null, LocalDate.now().minusDays(2),
				LocalDate.now().plusDays(58));
		Product grapes = new Product(null, 15, 30, "SEASONAL", "Grapes", null, LocalDate.now().plusDays(180),
				LocalDate.now().plusDays(240));

		List<Product> allProducts = List.of(usbCable, usbDongle, butter, milk, watermelon, grapes);
		Set<Product> orderItems = new HashSet<Product>(allProducts);
		Order order = createOrder(orderItems);
		productRepository.saveAll(allProducts);
		order = orderRepository.save(order);

		
		mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId()).contentType("application/json"))
				.andExpect(status().isOk());


		// NORMAL en stock  il faut décrémenté le stock
		Product savedUsbCable = productRepository.findById(usbCable.getId()).get();
		assertEquals(29, savedUsbCable.getAvailable());

		// NORMAL en rupture avec délai il faut notificatier le délai et le stock reste 0
		Product savedUsbDongle = productRepository.findById(usbDongle.getId()).get();
		assertEquals(0, savedUsbDongle.getAvailable());

		// EXPIRABLE en stock et non expiré  il faut décrémenté le stock
		Product savedButter = productRepository.findById(butter.getId()).get();
		assertEquals(29, savedButter.getAvailable());

		// EXPIRABLE expiré il faut  mettre le stock à 0 et notificatier l'expiration
		Product savedMilk = productRepository.findById(milk.getId()).get();
		assertEquals(0, savedMilk.getAvailable());

		// SEASONAL en saison avec stock il faut décrémenté le stock
		Product savedWatermelon = productRepository.findById(watermelon.getId()).get();
		assertEquals(29, savedWatermelon.getAvailable());

		// SEASONAL hors saison (pas encore commencée) il faut mettre le stock à 0 et notificatier la rupture
		Product savedGrapes = productRepository.findById(grapes.getId()).get();
		assertEquals(0, savedGrapes.getAvailable());

		Order resultOrder = orderRepository.findById(order.getId()).get();
		assertEquals(resultOrder.getId(), order.getId());

	}

	@Test
	void processOrder_Retourner404() throws Exception {
		mockMvc.perform(post("/orders/{orderId}/processOrder", 999999L).contentType("application/json"))
				.andExpect(status().isNotFound());
	}

	private static Order createOrder(Set<Product> products) {
		Order order = new Order();
		order.setItems(products);
		return order;
	}

}
