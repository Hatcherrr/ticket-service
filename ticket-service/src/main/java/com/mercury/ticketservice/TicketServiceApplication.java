package com.mercury.ticketservice;

import java.util.List;
import java.util.Scanner;

import com.mercury.ticketservice.beans.Seat;
import com.mercury.ticketservice.beans.SeatHold;
import com.mercury.ticketservice.services.impl.TicketServiceImpl;

public class TicketServiceApplication {

	private static Scanner input;

	// the higher the priority, the better the seats
	public static void printPriorities(TicketServiceImpl ticketService) {
		List<List<Seat>> seats = TicketServiceImpl.getSeats();
		for(List<Seat> seatList : seats) {
			System.out.println();
			for(Seat seat : seatList) {
				if(seat.getPriority() < 10)
					System.out.print("  " + seat.getPriority() + " ");
				else if(seat.getPriority() < 100)
					System.out.print(" " + seat.getPriority() + " ");
				else
					System.out.print(seat.getPriority() + " ");
			}
		}
		System.out.println();
	}
	
	public static void printSeats(TicketServiceImpl ticketService) {
		List<List<Seat>> seats = TicketServiceImpl.getSeats();
		System.out.println();
		System.out.println("------------------------------------------------------ S C R E E N -------------------------------------------------------");
		for(List<Seat> rowSeats : seats) {
			System.out.println();
			for(Seat seat : rowSeats) {
				char row = (char) ('A' + seat.getRow());
				int col = seat.getCol() + 1;
				if(seat.getStatus() == 0)
					System.out.print(row + "" + col +" ");
				else if(seat.getStatus() == 1) 
					System.out.print(" △ "); // Hold
				else
					System.out.print(" ▉ "); // Reserved
			}
		}
		System.out.println();
		System.out.println("△ - Holding   ▉ - Reserved");
		System.out.println();
	}
	
	public static void printSeatAmount(TicketServiceImpl ticketService) {
		System.out.println();
		System.out.println("Available seats: " + ticketService.numSeatsAvailable());
		System.out.println();
	}
	
	public static void bookSeparateSeats(TicketServiceImpl ticketService, int num, String email) {
		if(num > ticketService.numSeatsAvailable()) {
			System.out.println("\nSorry, seats are not enough!\n");
			return;
		}
		SeatHold seatHold = ticketService.findAndHoldSeparateSeats(num, email);
		if(seatHold != null) {
			System.out.print("You have booked seats: ");
			List<Seat> seatList = seatHold.getSeats();
			for(Seat seat : seatList) {
				char row = (char)('A' + seat.getRow());
				int col = seat.getCol() + 1;
				System.out.print(row + "" + col + " ");
			}
			System.out.println("\nSeat holding ID is " + seatHold.getId() + ". Expired in 15 minutes. Please pay soon!\n");
		}
	}
	
	public static void bookSeats(TicketServiceImpl ticketService) {
		System.out.println();
		System.out.print("How many seats you want to book? ");
		int num = -1;
		while(true) {
			input = new Scanner(System.in);
			try{
				num = input.nextInt();
				break;
			}catch(Exception e) {
				System.out.println("\nPlease input a number!\n");
				System.out.print("How many seats you want to book? ");
			}
		}
		if(num > ticketService.numSeatsAvailable()) {
			System.out.println("\nSorry, seats are not enough!\n");
			return;
		}
		System.out.print("Please input your email address: ");
		String email = input.next();
		while(email.indexOf('@') == -1 || email.indexOf(".com") == -1 || email.indexOf(".com") - email.indexOf('@') <= 1) {
			System.out.println("\nInvalid Email\n");
			System.out.print("Please input your email address: ");
			email = input.next();
		}
		System.out.println();
		SeatHold seatHold = ticketService.findAndHoldSeats(num, email);
		if(seatHold == null) {
			System.out.println("Sorry, book failed! Continues seats are not enough.\n");
			System.out.println("Do you want separate seats?(yes/no) ");
			String choice = input.next();
			while(!choice.equals("yes") && !choice.equals("no")) {
				System.out.print("\nPlease input only yes or no. ");
				choice = input.next();
			}
			if(choice.equals("yes")) {
				bookSeparateSeats(ticketService, num, email);
			}else {
				System.out.println();
			}
		}else {
			System.out.print("You have booked seats: ");
			List<Seat> seatList = seatHold.getSeats();
			for(Seat seat : seatList) {
				char row = (char)('A' + seat.getRow());
				int col = seat.getCol() + 1;
				System.out.print(row + "" + col + " ");
			}
			System.out.println("\nSeat holding ID is " + seatHold.getId() + ". Expired in 15 minutes. Please pay soon!\n");
		}
	}
	
	public static void reserveSeats(TicketServiceImpl ticketService) {
		System.out.println();
		System.out.print("Please input seat holding id: ");
		int id = -1;
		while(true) {
			input = new Scanner(System.in);
			try{
				id = input.nextInt();
				if(id > 0)
					break;
				else {
					System.out.println("\nSeat holding id not valid!\n");
					System.out.print("Please input seat holding id: ");
				}
			}catch(Exception e) {
				System.out.println("\nSeat holding id not valid!\n");
				System.out.print("Please input seat holding id: ");
			}
		}
		System.out.print("Please input your confirmation email: ");
		String email = input.next();
		while(email.indexOf('@') == -1 || email.indexOf(".com") == -1 || email.indexOf(".com") - email.indexOf('@') <= 1) {
			System.out.println("\nInvalid Email\n");
			System.out.print("Please input your email address: ");
			email = input.next();
		}
		String code = ticketService.reserveSeats(id, email);
		if(code == null) {
			System.out.println("\nSorry, not found! Wrong email address or holding seats expired.");
			System.out.println("Please try again.\n");
		}else {
			System.out.println("\nConfirmed! Here's your confirmation code: " + code);
			System.out.println("Please take care of it and show when you check in.\n");
		}
	}
	
	public static void main(String[] args) {
		TicketServiceImpl ticketService = new TicketServiceImpl();
		System.out.println("Welcome to Hatcher's Tickets Service!\n");
		while(true) {
			System.out.println("Main Menu:");
			System.out.println("1. Seats priorities distribution");
			System.out.println("2. Check the seats in Venue");
			System.out.println("3. Check number of available Seats");
			System.out.println("4. Book seats");
			System.out.println("5. Pay and Reserve seats");
			System.out.println("0. Exit");
			System.out.print("Please choose:");
			input = new Scanner(System.in);
			int choice = -1;
			try {
				choice = input.nextInt();
			}catch(Exception e) {
			}
			switch(choice) {
				case 1: printPriorities(ticketService); break;
				case 2: printSeats(ticketService); break;
				case 3: printSeatAmount(ticketService); break;
				case 4: bookSeats(ticketService); break;
				case 5: reserveSeats(ticketService); break;
				case 0: System.exit(0); break;
				default: System.out.println("\nInvalid Input!\n");break;
			}
		}
	}
	
}
