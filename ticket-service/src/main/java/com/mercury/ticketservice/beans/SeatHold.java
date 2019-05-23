package com.mercury.ticketservice.beans;

import java.util.Date;
import java.util.List;

public class SeatHold {

	private long id;
	
	private String email;
	
	private List<Seat> seats;
	
	private Date timestamp;

	public SeatHold() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SeatHold(long id, String email, List<Seat> seats, Date timestamp) {
		super();
		this.id = id;
		this.email = email;
		this.seats = seats;
		this.timestamp = timestamp;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public List<Seat> getSeats() {
		return seats;
	}

	public void setSeats(List<Seat> seats) {
		this.seats = seats;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "SeatHold [id=" + id + ", email=" + email + ", seats=" + seats + ", timestamp=" + timestamp + "]";
	}
	
}
