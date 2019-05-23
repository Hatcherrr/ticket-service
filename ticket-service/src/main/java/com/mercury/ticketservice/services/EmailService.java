package com.mercury.ticketservice.services;

public interface EmailService {
	/*
	 * send the confirmation email to customer
	 */
	void sendEmail(String code, String customerEmail);
}
