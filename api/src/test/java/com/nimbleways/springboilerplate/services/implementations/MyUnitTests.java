package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@UnitTest
public class MyUnitTests {

	@Mock
	private NotificationService notificationService;

	@Mock
	private ProductRepository productRepository;

	@InjectMocks
	private ProductService productService;

	@Test
	void produitNormal_enStock_decrementerLeStock() {

		Product product = new Product(null, 15, 10, "NORMAL", "USB Cable", null, null, null);
		Mockito.when(productRepository.save(product)).thenReturn(product);

		productService.handleProduct(product);

		assertEquals(9, product.getAvailable());
		Mockito.verify(productRepository).save(product);
		Mockito.verifyNoInteractions(notificationService);
	}

	@Test
	void produitNormal_ruptureDEStock_avecDelai_notifierLeDelai() {

		Product product = new Product(null, 15, 0, "NORMAL", "RJ45 Cable", null, null, null);
		Mockito.when(productRepository.save(product)).thenReturn(product);

		productService.handleProduct(product);

		assertEquals(0, product.getAvailable());
		assertEquals(15, product.getLeadTime());
		Mockito.verify(productRepository).save(product);
		Mockito.verify(notificationService).sendDelayNotification(15, "RJ45 Cable");
	}

	@Test
	void produitNormal_ruptureDEStock_sansDelai_neFaitRien() {

		Product product = new Product(null, 0, 0, "NORMAL", "Article Mystere", null, null, null);

		productService.handleProduct(product);

		Mockito.verifyNoInteractions(productRepository);
		Mockito.verifyNoInteractions(notificationService);
	}

	@Test
	void produitExpirable_enStockEtNonExpire_decrementerLeStock() {

		Product product = new Product(null, 10, 5, "EXPIRABLE", "Beurre", LocalDate.now().plusDays(10), null, null);
		Mockito.when(productRepository.save(product)).thenReturn(product);

		productService.handleProduct(product);

		assertEquals(4, product.getAvailable());
		Mockito.verifyNoInteractions(notificationService);
	}

	@Test
	void produitExpirable_expire_notifierExpirationEtMettreStockAZero() {

		LocalDate hier = LocalDate.now().minusDays(1);
		Product product = new Product(null, 90, 6, "EXPIRABLE", "Lait", hier, null, null);
		Mockito.when(productRepository.save(product)).thenReturn(product);

		productService.handleProduct(product);

		assertEquals(0, product.getAvailable());
		Mockito.verify(notificationService).sendExpirationNotification("Lait", hier);
		Mockito.verify(productRepository).save(product);
	}

	@Test
	void produitExpirable_ruptureDEStockMaisNonExpire_notifierExpirationEtMettreStockAZero() {

		LocalDate dateFuture = LocalDate.now().plusDays(5);
		Product product = new Product(null, 10, 0, "EXPIRABLE", "Yaourt", dateFuture, null, null);
		Mockito.when(productRepository.save(product)).thenReturn(product);

		productService.handleProduct(product);

		assertEquals(0, product.getAvailable());
		Mockito.verify(notificationService).sendExpirationNotification("Yaourt", dateFuture);
	}

	@Test
	void produitSaisonnier_enSaisonAvecStock_decrementerLeStock() {

		Product product = new Product(null, 15, 30, "SEASONAL", "Pastèque", null, LocalDate.now().minusDays(2),
				LocalDate.now().plusDays(58));
		Mockito.when(productRepository.save(product)).thenReturn(product);

		productService.handleProduct(product);

		assertEquals(29, product.getAvailable());
		Mockito.verifyNoInteractions(notificationService);
	}

	@Test
	void produitSaisonnier_saisonNonCommencee_notifierRuptureEtMettreStockAZero() {

		Product product = new Product(null, 15, 30, "SEASONAL", "Raisin", null, LocalDate.now().plusDays(180),
				LocalDate.now().plusDays(240));
		Mockito.when(productRepository.save(product)).thenReturn(product);

		productService.handleProduct(product);

		assertEquals(0, product.getAvailable());
		Mockito.verify(notificationService).sendOutOfStockNotification("Raisin");
	}

	@Test
	void produitSaisonnier_ruptureDEStockDelaiCompatibleAvecSaison_notifierLeDelai() {
		// la saison est active, stock = 0, délai de réappro tient dans la saison
		Product product = new Product(null, 5, 0, "SEASONAL", "Pêche", null, LocalDate.now().minusDays(1),
				LocalDate.now().plusDays(30));
		Mockito.when(productRepository.save(product)).thenReturn(product);

		productService.handleProduct(product);

		Mockito.verify(notificationService).sendDelayNotification(5, "Pêche");
		Mockito.verify(productRepository).save(product);
	}

	@Test
	void produitSaisonnier_ruptureDEStockDelaiDepasse_notifierRuptureEtMettreStockAZero() {
		// la saison se termine dans 3 jours mais le délai de réappro est 10 jours
		Product product = new Product(null, 10, 0, "SEASONAL", "Fraise", null, LocalDate.now().minusDays(10),
				LocalDate.now().plusDays(3));
		Mockito.when(productRepository.save(product)).thenReturn(product);

		productService.handleProduct(product);

		assertEquals(0, product.getAvailable());
		Mockito.verify(notificationService).sendOutOfStockNotification("Fraise");
	}
}