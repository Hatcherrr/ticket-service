package com.mercury.ticketservice;

import java.util.List;
import java.util.Scanner;

import com.mercury.ticketservice.beans.Seat;
import com.mercury.ticketservice.beans.SeatHold;
import com.mercury.ticketservice.services.impl.TicketServiceImpl;

public class TicketServiceApplication {

	private static Scanner input;
	
	private static TicketServiceImpl ticketService = new TicketServiceImpl();

	// the higher the priority, the better the seats
	public static void printPriorities() {
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
	
	public static void printSeats() {
		List<List<Seat>> seats = TicketServiceImpl.getSeats();
		System.out.println("\n---------------------------------------------------------- S C R E E N ------------------------------------------------------------");
		for(List<Seat> rowSeats : seats) {
			System.out.println();
			for(Seat seat : rowSeats) {
				char row = (char) ('A' + seat.getRow());
				int col = seat.getCol() + 1;
				if(seat.getStatus() == 0) {
					System.out.format("%c%02d ", row, col);
				}
				else if(seat.getStatus() == 1) {
					System.out.format("%-4s", " Δ"); // Hold
				}
				else {
					System.out.format("%-4s", " ◆"); // Reserved
				}
			}
		}
		System.out.println("\nΔ - Holding   ◆ - Reserved\n");
	}
	
	public static void printSeatAmount() {
		System.out.println("\nAvailable seats: " + ticketService.numSeatsAvailable());
		System.out.println();
	}
	
	public static void bookSeparateSeats(int num, String email) {
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
		}else {
			System.out.println("Sorry, book failed!\n");
		}
	}
	
	public static void bookSeats() {
		System.out.println();
		System.out.print("How many seats you want to book?(input 0 back to the main menu) ");
		int num = -1;
		while(true) {
			input = new Scanner(System.in);
			try{
				num = input.nextInt();
				if(num > 0)
					break;
				else if(num == 0) {
					System.out.println();
					return;
				}else {
					System.out.println("\nInvalid number\n");
					System.out.print("How many seats you want to book?(input 0 back to the main menu) ");
				}
			}catch(Exception e) {
				System.out.println("\nPlease input a number!\n");
				System.out.print("How many seats you want to book?(input 0 back to the main menu) ");
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
			System.out.print("Do you want separate seats?(yes/no) ");
			String choice = input.next();
			while(!choice.equals("yes") && !choice.equals("no")) {
				System.out.print("\nPlease input only yes or no. ");
				choice = input.next();
			}
			if(choice.equals("yes")) {
				bookSeparateSeats(num, email);
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
	
	public static void reserveSeats() {
		System.out.print("\nPlease input seat holding id(input 0 for exist): ");
		int id = -1;
		while(true) {
			input = new Scanner(System.in);
			try{
				id = input.nextInt();
				if(id > 0)
					break;
				else if(id == 0) {
					System.out.println();
					return;
				}else {
					System.out.println("\nSeat holding id not valid!\n");
					System.out.print("Please input seat holding id(input 0 for exist): ");
				}
			}catch(Exception e) {
				System.out.println("\nSeat holding id not valid!\n");
				System.out.print("Please input seat holding id(input 0 for exist): ");
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
				case 1: printPriorities(); break;
				case 2: printSeats(); break;
				case 3: printSeatAmount(); break;
				case 4: bookSeats(); break;
				case 5: reserveSeats(); break;
				case 0: System.exit(0); break;
				default: System.out.println("\nInvalid Input!\n");break;
			}
		}
	}
	
}
