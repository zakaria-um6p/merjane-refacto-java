package com.nimbleways.springboilerplate.services.implementations;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.interfaces.IProductService;

@Service
public class ProductService implements IProductService {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private NotificationService notificationService;

	@Override
	public void handleProduct(Product product) {
		switch (product.getType()) {
		case "NORMAL":
			handleNormalProduct(product);
			break;
		case "SEASONAL":
			handleSeasonalProduct(product);
			break;
		case "EXPIRABLE":
			handleExpirableProduct(product);
			break;
		default:
			throw new IllegalArgumentException("Type de produit inconnu : " + product.getType());
		}

	}

	private void handleNormalProduct(Product product) {
		if (product.getAvailable() > 0) {
			decrementStock(product);
		} else if (product.getLeadTime() > 0) {
			notifyDelay(product.getLeadTime(), product);
		}
	}

	private void handleSeasonalProduct(Product product) {
		LocalDate today = LocalDate.now();
		boolean isInSeason = !today.isBefore(product.getSeasonStartDate())
				&& today.isBefore(product.getSeasonEndDate());

		if (isInSeason && product.getAvailable() > 0) {
			decrementStock(product);
		} else {
			handleSeasonalOutOfStock(product);
		}
	}

	private void handleExpirableProduct(Product product) {
		boolean isNotExpired = product.getExpiryDate().isAfter(LocalDate.now());

		if (product.getAvailable() > 0 && isNotExpired) {
			decrementStock(product);
		} else {
			notificationService.sendExpirationNotification(product.getName(), product.getExpiryDate());
			product.setAvailable(0);
			productRepository.save(product);
		}
	}

	private void handleSeasonalOutOfStock(Product product) {
		LocalDate today = LocalDate.now();
		boolean seasonHasNotStarted = product.getSeasonStartDate().isAfter(today);
		boolean leadTimeExceedsSeason = today.plusDays(product.getLeadTime()).isAfter(product.getSeasonEndDate());

		if (leadTimeExceedsSeason || seasonHasNotStarted) {
			notificationService.sendOutOfStockNotification(product.getName());
			product.setAvailable(0);
			productRepository.save(product);
		} else {
			notifyDelay(product.getLeadTime(), product);
		}

	}

	private void decrementStock(Product product) {
		product.setAvailable(product.getAvailable() - 1);
		productRepository.save(product);
	}

	private void notifyDelay(int leadTime, Product product) {
		product.setLeadTime(leadTime);
		productRepository.save(product);
		notificationService.sendDelayNotification(leadTime, product.getName());
	}

}