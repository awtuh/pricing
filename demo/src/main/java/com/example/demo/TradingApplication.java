package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Bean;

@RestController
@SpringBootApplication
public class TradingApplication {

	public static void main(String[] args) {
		SpringApplication.run(TradingApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@GetMapping("/hello")
	public String hello(@RequestParam(value = "name", defaultValue = "zizi") String name) {
		return String.format("caca %s!", name);
	}
	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/pricing")
	public String getPricing() {
		try {
			RestTemplate restTemplate = new RestTemplate();
			// API CoinGecko pour le prix de l'or (PAX Gold)
			String url = "https://api.coingecko.com/api/v3/simple/price?ids=pax-gold&vs_currencies=usd";
			String response = restTemplate.getForObject(url, String.class);

			return formatGoldPrice(response);
		} catch (Exception e) {
			return "Erreur lors de la récupération du prix de l'or: " + e.getMessage();
		}
	}

	private String formatGoldPrice(String jsonResponse) {
		try {
			// Extraire le prix de PAX Gold (représente le prix de l'or)
			String searchPattern = "\"pax-gold\":{\"usd\":";
			int startIndex = jsonResponse.indexOf(searchPattern);
			if (startIndex == -1) {
				return "Prix de l'or non trouvé dans la réponse";
			}

			startIndex += searchPattern.length();
			int endIndex = jsonResponse.indexOf("}", startIndex);
			String priceStr = jsonResponse.substring(startIndex, endIndex);

			Double goldPrice = Double.parseDouble(priceStr);

			return goldPrice.toString();

		} catch (Exception e) {
			return "Erreur de parsing: " + e.getMessage() + "\nRéponse: " + jsonResponse;
		}
	}
}