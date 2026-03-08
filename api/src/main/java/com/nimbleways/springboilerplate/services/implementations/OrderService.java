package com.nimbleways.springboilerplate.services.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.services.interfaces.IOrderService;

@Service
public class OrderService implements IOrderService{
	
	@Autowired
	private OrderRepository orderRepository;

	@Override
	public Order getOrder(Long orderId) {
		return orderRepository.findById(orderId).orElse(null);
	}

}
