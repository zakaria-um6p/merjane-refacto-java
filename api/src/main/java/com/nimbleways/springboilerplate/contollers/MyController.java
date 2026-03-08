package com.nimbleways.springboilerplate.contollers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.services.implementations.OrderService;
import com.nimbleways.springboilerplate.services.interfaces.IProductService;

@RestController
@RequestMapping("/orders")
public class MyController {
	
	@Autowired
    private IProductService productService;
	
	@Autowired
	private OrderService orderService;
	
	@PostMapping("{orderId}/processOrder")
    @ResponseStatus(HttpStatus.OK)
    public ProcessOrderResponse processOrder(@PathVariable Long orderId) {
		Order order = orderService.getOrder(orderId);
		if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucun order avec l'identifiant: " + orderId);
        }
        order.getItems().forEach(productService::handleProduct);
        return new ProcessOrderResponse(order.getId());
    }
	
}
