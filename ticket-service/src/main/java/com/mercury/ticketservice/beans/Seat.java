package com.mercury.ticketservice.beans;

public class Seat implements Comparable<Seat>{
	
	private int row;
	
	private int col;
	
	private int priority;
	
	/*
	 * 0 - Available
	 * 1 - Hold
	 * 2 - Reserved
	 */
	private int status;

	public Seat() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Seat(int row, int col, int priority, int status) {
		super();
		this.row = row;
		this.col = col;
		this.priority = priority;
		this.status = status;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Seat [row=" + row + ", col=" + col + ", priority=" + priority + ", status=" + status + "]";
	}

	@Override
	public int compareTo(Seat seat) {
		return seat.priority - this.priority;
	}
}
