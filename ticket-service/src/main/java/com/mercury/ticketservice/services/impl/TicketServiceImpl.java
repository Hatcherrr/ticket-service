package com.mercury.ticketservice.services.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

import com.mercury.ticketservice.beans.Seat;
import com.mercury.ticketservice.beans.SeatHold;
import com.mercury.ticketservice.services.TicketService;

public class TicketServiceImpl implements TicketService {

	private static final int ROW = 9;
	
	private static final int COLUMN = 33;
	
	private static final int[] rowPriority = {0, 1, 2, 6, 8, 7, 5, 4, 3}; // different row has different priority
	
	private static long EXPIRE_TIME = 15 * 60 * 1000; // expired in 15 mins
	
	private static Queue<Seat> avilableSeats;
	
	private static List<List<Seat>> seats;
	
	private static List<SeatHold> holds;
	
	private static long seatHoldId = 1; // id of hold
	
	/*
	 * each seat has its priority, start from 0
	 * the middle part has the highest
	 * the back part has higher priority than front
	 * you can see in the priority distribution in main method
	 */
	static {
		// initialize seats with priority
		seats = new CopyOnWriteArrayList<>();
		for(int i = 0; i < ROW; i++) {
			List<Seat> rowSeats = new CopyOnWriteArrayList<>();
			int left = 0;
			int right = COLUMN - 1;
			int priority = rowPriority[i] * COLUMN; // row priority use in here
			for(int j = 0; left <= right; j++) { // give every seat in the row a priority, from the side to the middle
				if(j % 2 == 0) {
					rowSeats.add(new Seat(i, left++, priority++, 0));
				}else {
					rowSeats.add(new Seat(i, right--, priority++, 0));
				}
			}
			Collections.sort(rowSeats, (seat1, seat2) -> seat1.getCol() - seat2.getCol()); // sort seats by column
			seats.add(rowSeats);
		}
		
		// initialize available seats, using PriorityQueue
		avilableSeats = seats.stream()
				.flatMap(rowSeats -> rowSeats.stream())
				.collect(Collectors.toCollection(PriorityBlockingQueue::new));
		
		// initialize hold list
		holds = new CopyOnWriteArrayList<>();
	}
	
	// release expired Holds
	private synchronized void releaseHolds() {
		if(holds.size() == 0)
			return;
		long systemTime = new Date().getTime(); // get current system time
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
		Collections.sort(seatList, (seat1, seat2) -> seat1.getCol() - seat2.getCol()); // sort by column
		Seat temp = seatList.get(0);
		int size = seatList.size();
		for(int i = 1; i < size; i++) {
			if(temp.getRow() != seatList.get(i).getRow() || seatList.get(i).getCol() - temp.getCol() != 1) // if row doesn't equal or column doesn't continues 
				return false;
			temp = seatList.get(i);
		}
		return true;
	}
	
	/*
	 * find n continues seats
	 * if exists, return List<Seat>
	 * else return null
	 */
	public synchronized List<Seat> findContinuesSeats(int n){
		Queue<Seat> tempAvilableSeats = avilableSeats.stream()
				.collect(Collectors.toCollection(PriorityBlockingQueue::new));
		List<Seat> resultSeats = new LinkedList<>();
		while(tempAvilableSeats.size() > 0) {
			if(resultSeats.size() == n) {
				return resultSeats;
			}
			Seat seat = tempAvilableSeats.poll();
			resultSeats.add(seat);
			if(!isContinues(resultSeats)) {
				resultSeats.clear(); // because avilableSeats are sorted by priority, if seats are not continues, clear
				resultSeats.add(seat); // then start from current seat to find continues seats
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

	/*
	 *  find seats which are separate
	 *  directly poll out from avilableSeats
	 */
	public synchronized SeatHold findAndHoldSeparateSeats(int numSeats, String customerEmail) {
		if(numSeats <= 0 || numSeats > numSeatsAvailable() || customerEmail == null || customerEmail.length() < 7)
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
	 * minimum length of email is 7, example: a@b.com
	 * Best Seats: the continues seats which has the highest priorities
	 */
	@Override
	public synchronized SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
		if(numSeats <= 0 || numSeats > numSeatsAvailable() || customerEmail == null || customerEmail.length() < 7)
			return null;
		List<Seat> resultSeats = null;
		// if numSeats is bigger than 1 row, means need split the seats into multiple rows
		if(numSeats > COLUMN) {
			List<Seat> hugeSeatsList = new ArrayList<>();
			List<Seat> hugeSeats = findContinuesSeats(COLUMN);
			int num = numSeats - COLUMN;
			while(num > 0) {
				if(hugeSeats != null) {
					avilableSeats.removeAll(hugeSeats);
					hugeSeatsList.addAll(hugeSeats);
					if(num > COLUMN)
						hugeSeats = findContinuesSeats(COLUMN);
					else
						hugeSeats = findContinuesSeats(num);
					num -= COLUMN;
				}else {
					avilableSeats.addAll(hugeSeatsList);
					hugeSeatsList.clear();
					break;
				}
			}
			// last loop
			if(hugeSeats != null)
				hugeSeatsList.addAll(hugeSeats);
			else {
				avilableSeats.addAll(hugeSeatsList);
				hugeSeatsList.clear();
			}
			resultSeats = hugeSeatsList.stream()
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
	public synchronized String reserveSeats(int seatHoldId, String customerEmail) {
		if(seatHoldId <= 0 || customerEmail == null || customerEmail.length() < 7)
			return null;
		releaseHolds();
		int i = 0;
		int size = holds.size();
		SeatHold seatHold = null;
		// find hold by seatHoldId
		while(i < size) {
			if(holds.get(i).getId() == seatHoldId) {
				seatHold = holds.get(i);
				break;
			}
		}
		if(seatHold == null || !seatHold.getEmail().equals(customerEmail))
			return null;
		
		// seatHold found, change the status of every seat and remove from holding list
		List<Seat> seatList = seatHold.getSeats();
		seatList.stream().forEach(seat -> {
			int row = seat.getRow();
			int col = seat.getCol();
			seats.get(row).get(col).setStatus(2);
		});
		holds.remove(seatHold);
		
		// create code
		StringBuilder sb = new StringBuilder();
		for(Seat s : seatList) {
			int row = s.getRow() + 1;
			int col = s.getCol() + 1;
			sb.append(row).append(col);
		}
		sb.append(seatList.size());
		return new BigInteger(sb.toString(),10).toString(32).toUpperCase();
		
		// create code
//		long codeRow = 0; // 9 bits, each bit represent the row number of seat
//		long codeCol = 0; // 33 bits, each bit represent the column number of seat
//		List<Integer> rowList = seatList.stream()
//				.map(Seat::getRow)
//				.distinct()
//				.collect(Collectors.toList());
//		for(int move : rowList)
//			codeRow += 1l << move;
//		List<Integer> colList = seatList.stream()
//				.map(Seat::getCol)
//				.distinct()
//				.collect(Collectors.toList());
//		System.out.println(colList);
//		for(int move : colList)
//			codeCol += 1l << move;
//		
//		// long is 64 bits. Totally we use 51 bits as code, including seats amount
//		// total amount of seats is 297, which is 9 bits
//		System.out.println(codeRow + "-" + codeCol);
//		long code = (codeRow << 33 + 9) + (codeCol << 9) + seatList.size(); // code of row left shift 42 bits + code of column left shift 9 bits + amount of seats
//																		// in this way we can easily reverse and get the seats from this code
//		return "" + code;
	}

	/*
	 * minimum size of confirmation code is 13
	 * use the code to reverse and get the seats
	 */
//	public List<Seat> checkinSeats(String confirmCode, String customerEmail){
//		if(confirmCode == null || confirmCode.length() < 13 || customerEmail == null || customerEmail.length() < 7)
//			return null;
//		long code = Long.parseLong(confirmCode);
//		int num = (int) (code & 511l);
//		long rowCode = (code & 511l << 42) >>> 42;
//		long colCode = (code & (1l << 33) - 1 << 9) >>> 9;
//		System.out.println(colCode);
//		List<Seat> seatsList = new ArrayList<>();
//		List<Integer> rowList = new ArrayList<>();
//		List<Integer> colList = new ArrayList<>();
//		for(int i = 0; i < 9; i++) {
//			if(rowCode % 2 != 0)
//				rowList.add(i);
//			rowCode /= 2;
//		}
//		System.out.println(rowList);
//		for(int i = 0; i < 33; i++) {
//			if(colCode % 2 != 0)
//				colList.add(i);
//			colCode /= 2;
//		}
//		System.out.println(colList);
//		for(int i : rowList)
//			for(int j : colList)
//				seatsList.add(seats.get(i).get(j));
//		System.out.println(seatsList.size());
//		if(seatsList.size() != num)
//			return null;
//		return seatsList;
//	}
	
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
