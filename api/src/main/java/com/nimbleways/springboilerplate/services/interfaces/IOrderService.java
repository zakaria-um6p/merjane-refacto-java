package com.nimbleways.springboilerplate.services.interfaces;

import com.nimbleways.springboilerplate.entities.Order;

public interface IOrderService {

	public Order getOrder(Long orderId);
}
