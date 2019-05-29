package com.mercury.ticketservice.services.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Collectors;

import com.mercury.ticketservice.beans.Seat;
import com.mercury.ticketservice.beans.SeatHold;
import com.mercury.ticketservice.services.TicketService;

public class TicketServiceImpl implements TicketService {

	private static final int ROW = 9;
	
	private static final int COLUMN = 33;
	
	private static final int[] rowPriority;
	
	private static long EXPIRE_TIME = 15 * 60 * 1000; // expired in 15 mins
	
	private static Queue<Seat> avilableSeats;
	
	private static List<List<Seat>> seats;
	
	private static List<SeatHold> holds;
	
	private static long seatHoldId = 1;
	
	/*
	 * each seat has its priority
	 * the middle part has the highest
	 * the back part has higher priority than front
	 * you can see in the priority distribution in main method
	 */
	static {
		rowPriority = new int[]{0, 1, 2, 6, 8, 7, 5, 4, 3};
		// initialize seats with priority
		seats = new LinkedList<>();
		for(int i = 0; i < ROW; i++) {
			List<Seat> rowSeats = new LinkedList<>();
			int left = 0;
			int right = COLUMN - 1;
			int priority = rowPriority[i] * COLUMN;
			for(int j = 0; left <= right; j++) {
				if(j % 2 == 0) {
					rowSeats.add(new Seat(i, left++, priority++, 0));
				}
				else {
					rowSeats.add(new Seat(i, right--, priority++, 0));
				}
			}
			Collections.sort(rowSeats, (seat1, seat2) -> seat1.getCol() - seat2.getCol());
			seats.add(rowSeats);
		}
		
		// initialize available seats
		avilableSeats = seats.stream()
				.flatMap(rowSeats -> rowSeats.stream())
				.collect(Collectors.toCollection(PriorityQueue::new));
		
		// initialize hold list
		holds = new ArrayList<>();
	}
	
	// release expired Holds
	private void releaseHolds() {
		if(holds.size() == 0)
			return;
		long systemTime = new Date().getTime();
		List<SeatHold> tempHolds = holds.stream()
				.filter(hold -> systemTime - hold.getTimestamp().getTime() >= EXPIRE_TIME)
				.collect(Collectors.toList());
		if(tempHolds.size() == 0)
			return;
		tempHolds.stream().forEach(hold -> {
			hold.getSeats().stream().forEach(seat -> {
				int row = seat.getRow();
				int col = seat.getCol();
				Seat s = seats.get(row).get(col);
				s.setStatus(0);
				avilableSeats.add(s);
			});
		});
		holds.removeAll(tempHolds);
	}
	
	// check if the seats are continues
	public boolean isContinues(List<Seat> seatList) {
		Collections.sort(seatList, (seat1, seat2) -> seat1.getCol() - seat2.getCol());
		Seat temp = seatList.get(0);
		int size = seatList.size();
		for(int i = 1; i < size; i++) {
			if(temp.getRow() != seatList.get(i).getRow() || seatList.get(i).getCol() - temp.getCol() != 1)
				return false;
			temp = seatList.get(i);
		}
		return true;
	}
	
	/*
	 * find n continues seats
	 * if exists, return List<Seat>
	 * else return null;
	 */
	public List<Seat> findContinuesSeats(int n){
		Queue<Seat> tempAvilableSeats = avilableSeats.stream()
				.collect(Collectors.toCollection(PriorityQueue::new));
		List<Seat> resultSeats = new LinkedList<>();
		while(tempAvilableSeats.size() > 0) {
			if(resultSeats.size() == n) {
				return resultSeats;
			}
			Seat seat = tempAvilableSeats.poll();
			resultSeats.add(seat);
			if(!isContinues(resultSeats)) {
				resultSeats.clear(); // if seats are not continues, clear
				resultSeats.add(seat);
			}
		}
		// if they're last n continues seats
		if(resultSeats.size() == n)
			return resultSeats;
		return null;
	}
	
	@Override
	public int numSeatsAvailable() {
		releaseHolds();
		return avilableSeats.size();
	}

	// find seats which are separate
	public SeatHold findAndHoldSeparateSeats(int numSeats, String customerEmail) {
		if(numSeats <= 0 || numSeats > numSeatsAvailable())
			return null;
		List<Seat> resultSeats = new ArrayList<>();
		for(int i = 0; i < numSeats; i++) {
			Seat seat = avilableSeats.poll();
			int row = seat.getRow();
			int col = seat.getCol();
			seats.get(row).get(col).setStatus(1);
			resultSeats.add(seat);
		}
		SeatHold seatHold = new SeatHold(seatHoldId++, customerEmail, resultSeats, new Date());
		holds.add(seatHold);
		return seatHold;
	}
	
	/*
	 * Best Seats: the continues seats which has the highest priorities
	 */
	@Override
	public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
		if(numSeats <= 0 || numSeats > numSeatsAvailable())
			return null;
		List<Seat> resultSeats = null;
		// if numSeats is bigger than 1 row, means need split the seats into multiple rows
		if(numSeats > COLUMN) {
			List<List<Seat>> hugeSeatsList = new ArrayList<>();
			List<Seat> hugeSeats = findContinuesSeats(COLUMN);
			int num = numSeats - COLUMN;
			while(num > 0) {
				if(hugeSeats != null) {
					avilableSeats.removeAll(hugeSeats);
					hugeSeatsList.add(hugeSeats);
					if(num > COLUMN)
						hugeSeats = findContinuesSeats(COLUMN);
					else
						hugeSeats = findContinuesSeats(num);
					num -= COLUMN;
				}else {
					for(List<Seat> addBackSeats : hugeSeatsList)
						avilableSeats.addAll(addBackSeats);
					hugeSeatsList.clear();
					break;
				}
			}
			// last loop
			if(hugeSeats != null)
				hugeSeatsList.add(hugeSeats);
			else {
				for(List<Seat> addBackSeats : hugeSeatsList)
					avilableSeats.addAll(addBackSeats);
				hugeSeatsList.clear();
			}
			resultSeats = hugeSeatsList.stream()
					.flatMap(rowSeats -> rowSeats.stream())
					.collect(Collectors.toList());
		}else {
			resultSeats = findContinuesSeats(numSeats);
		}
		
		if(resultSeats == null || resultSeats.size() == 0)
			return null;
		
		// change status, and remove the seats from priority queue
		resultSeats.stream().forEach(seat -> {
			int row = seat.getRow();
			int col = seat.getCol();
			seats.get(row).get(col).setStatus(1);
			avilableSeats.remove(seat);
		});
		SeatHold seatHold = new SeatHold(seatHoldId++, customerEmail, resultSeats, new Date());
		holds.add(seatHold);
		return seatHold;
	}

	@Override
	public String reserveSeats(int seatHoldId, String customerEmail) {
		if(seatHoldId <= 0 || customerEmail.length() == 0 || customerEmail == null)
			return null;
		releaseHolds();
		int i = 0;
		int size = holds.size();
		SeatHold seatHold = null;
		while(i < size) {
			if(holds.get(i).getId() == seatHoldId) {
				seatHold = holds.get(i);
				break;
			}
		}
		if(seatHold == null || !seatHold.getEmail().equals(customerEmail))
			return null;
		
		// seathold found, change the status of every seat and remove from holding list
		List<Seat> seatList = seatHold.getSeats();
		seatList.stream().forEach(seat -> {
			int row = seat.getRow();
			int col = seat.getCol();
			seats.get(row).get(col).setStatus(2);
		});
		holds.remove(seatHold);
		
		// calculate the confirmation code
		long code = 0;
		for(Seat seat : seatList) {
			int row = seat.getRow();
			int col = seat.getCol();
			code += row * 100 + col;
			code *= 1000;
		}
		return "" + code;
	}

	public static Queue<Seat> getAvilableSeats() {
		return avilableSeats;
	}

	public static void setAvilableSeats(Queue<Seat> avilableSeats) {
		TicketServiceImpl.avilableSeats = avilableSeats;
	}

	public static List<List<Seat>> getSeats() {
		return seats;
	}

	public static void setSeats(List<List<Seat>> seats) {
		TicketServiceImpl.seats = seats;
	}

	public static List<SeatHold> getHolds() {
		return holds;
	}

	public static void setHolds(List<SeatHold> holds) {
		TicketServiceImpl.holds = holds;
	}

	public static long getEXPIRE_TIME() {
		return EXPIRE_TIME;
	}

	public static void setEXPIRE_TIME(long eXPIRE_TIME) {
		EXPIRE_TIME = eXPIRE_TIME;
	}
}
