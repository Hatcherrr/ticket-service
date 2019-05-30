package com.mercury.tickerservice;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.mercury.ticketservice.beans.Seat;
import com.mercury.ticketservice.beans.SeatHold;
import com.mercury.ticketservice.services.impl.TicketServiceImpl;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TickServiceTest {
	
	private TicketServiceImpl ticketService;
	
	@Before
	public void setUp() throws Exception {
		ticketService = new TicketServiceImpl();
	}

	/*
	 * Test the return confirmation code
	 * return seats holding id 1
	 */
	@Test
	public void testConfirmationCode() {
		SeatHold hold = ticketService.findAndHoldSeats(2, "hatcher.zhao.java@gmail.com");
		String confirm = ticketService.reserveSeats(hold.getId(), "hatcher.zhao.java@gmail.com");
		
		Assert.assertTrue(confirm.equals("4TTSE"));
	}
	
	/*
	 * Test if email is wrong during reservation
	 */
	@Test
	public void testConfirmationEmail() {
		ticketService.findAndHoldSeats(5, "abcdefg@gmail.com");
		String confirm = ticketService.reserveSeats(2, "hatcher.zhao.java@gmail.com");
		Assert.assertTrue(confirm == null);
	}
	
	@Test
	public void testSeatHold() {
		SeatHold seatHold = ticketService.findAndHoldSeats(4, "hatcher.zhao.java@gmail.com");
		Assert.assertEquals(seatHold.getId(), 3);
		Assert.assertEquals(seatHold.getSeats().get(0).getRow(), 3); // seats: D16--D19. index of D is 3
		Assert.assertEquals(seatHold.getSeats().get(0).getCol(), 15); // first seat is D16
		Assert.assertEquals(seatHold.getSeats().get(3).getCol(), 18);
	}
	
	@Test
	public void testSeatHold2() {
		SeatHold seatHold = ticketService.findAndHoldSeats(40, "test@gmail.com"); // seats: G1--G33
																				//        H14--H20
		Assert.assertEquals(seatHold.getId(), 4);
		Assert.assertEquals(seatHold.getSeats().get(32).getRow(), 6); // 33th seat, index of G is 6
		Assert.assertEquals(seatHold.getSeats().get(32).getCol(), 32);
		Assert.assertEquals(seatHold.getSeats().get(33).getCol(), 13); // H14
	}
	
	/*
	 * book more than available seats
	 */
	@Test
	public void testSeatHold3() {
		SeatHold seatHold = ticketService.findAndHoldSeats(290, "morethan@gmail.com"); // 246 available seats
		Assert.assertEquals(seatHold, null);
	}
	
	/*
	 * currently, 4 rows occupied
	 * 9 - 4 = 5 whole rows available
	 * so if book 6 * 33 = 198 seats
	 * only can occupy 5 whole rows, so book failed
	 */
	@Test
	public void testSeatHold4() {
		SeatHold seatHold = ticketService.findAndHoldSeats(198, "overrows@gmail.com");
		Assert.assertEquals(seatHold, null);
	}
	
	/*
	 * already hold or reserved 40 + 5 + 4 + 2 = 51 seats
	 */
	@Test
	public void testTotalTickets() {
		long count = TicketServiceImpl.getSeats().stream()
				.flatMap(rowSeats -> rowSeats.stream())
				.count();
		Assert.assertTrue(ticketService.numSeatsAvailable() == count - 51);
	}
	
	/*
	 * 3 holding
	 */
	@Test
	public void zTestHoldAmount() {
		int count = TicketServiceImpl.getHolds().size();
		Assert.assertEquals(count, 3);
	}
	
	/*
	 * holding 49 seats now
	 */
	@Test
	public void zTestHoldAmount2() {
		long count = TicketServiceImpl.getHolds().stream()
				.flatMap(hold -> hold.getSeats().stream())
				.count();
		Assert.assertEquals(count, 49);
	}
	
	/*
	 * set the EXPIRE_TIME to 1ms
	 * then let the thread sleep 1s, make sure all holds will be expired
	 * so it will release all holds
	 * only 2 seats are reserved
	 * so total available seats will be 295
	 */
	@Test
	public void zTestHoldExpired() {
		TicketServiceImpl.setEXPIRE_TIME(1);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		int count = ticketService.numSeatsAvailable();
		Assert.assertTrue(count == 295);
	}
	
	/*
	 *  test multi-threading case with sleep()
	 *  at the beginning, there're 295 available seats
	 */
	@Test
	public void zzTestConcurrent() throws InterruptedException {
		TicketServiceImpl.setEXPIRE_TIME(15 * 60 * 1000); // set expire time back to 15 minutes
		
		Thread t1 = new Thread(() -> {
			try {
				Thread.sleep(1000); // make sure the second thread get the lock first
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			int count = ticketService.numSeatsAvailable();
			Assert.assertEquals(count, 293);
			ticketService.findAndHoldSeats(10, "thread1@testcase.com");
			count = ticketService.numSeatsAvailable();
			Assert.assertEquals(count, 283);
		});
		
		Thread t2 = new Thread(() -> {
			ticketService.findAndHoldSeats(2, "thread2@testcase.com");
		});
		
		t1.start();
		t2.start();
		
		t1.join();
		t2.join();
	}
	
	/*
	 * test multi-threading without sleep()
	 * at the beginning, there're 283 available seats
	 */
	@Test
	public void zzTestConcurrent2() throws InterruptedException {
		Thread t3 = new Thread(() -> {
			int count = ticketService.numSeatsAvailable();
			Assert.assertTrue(count == 283 || count == 281); // not guaranteed which thread will execute first
			ticketService.findAndHoldSeats(10, "thread3@testcase.com");
			count = ticketService.numSeatsAvailable();
			Assert.assertTrue(count == 273 || count == 271);
		});
		
		Thread t4 = new Thread(() -> {
			int count = ticketService.numSeatsAvailable();
			Assert.assertTrue(count == 283 || count == 273);
			ticketService.findAndHoldSeats(2, "thread4@testcase.com");
			count = ticketService.numSeatsAvailable();
			Assert.assertTrue(count == 281 || count == 271);
		});
		
		t3.start();
		t4.start();
		
		t3.join();
		t4.join();
	}
}
