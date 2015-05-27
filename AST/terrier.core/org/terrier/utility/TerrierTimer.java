/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org 
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.ac.uk/
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is TerrierTimer.java.
 *
 * The Original Code is Copyright (C) 2004-2011 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Ben He <ben{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.utility;
/**
 * This class implements a timer.
 */
public class TerrierTimer {
	/** The starting system time in millisecond. */ 
	private long startingTime;
	
	/** The processing time in minutes. */
	private int minutes;
	
	/** The processing time in seconds. */
	private int seconds;
	
	/** The total number of items to process in a task. */
	protected double total;

	protected double percentage;
	
	/**
	 * Default constructor.
	 *
	 */
	public TerrierTimer(){
		this.start();
	}
	
	/** Start the timer.
	 */
	public void start(){
		this.startingTime = System.currentTimeMillis();
		this.minutes = 0;
		this.seconds = 0;
		this.percentage = 0;
	}
	
	/**
	 * Reset the timer.
	 *
	 */
	public void restart(){
		this.start();
	}
	/**
	 * Compute the processing time.
	 *
	 */
	public void setBreakPoint(){
		long processingEnd = System.currentTimeMillis();
		long processingTime = (processingEnd - this.startingTime) / 1000;
		minutes = (int) (processingTime / 60.0d);
		seconds = (int) (processingTime % 60.0d);
	}
	/** Get the processing time in minutes. */
	public int getMinutes(){
		return this.minutes;
	}
	/** Set the overall quantitative workload of the task. */
	public void setTotalNumber(double _total){
		this.total = _total;
	}
	/**
	 * Estimate the remaining time.
	 * @param finished The quantitative finished workload.
	 */
	public void setRemainingTime(double finished){
		long processingEnd = System.currentTimeMillis();
		long processingTime = (processingEnd - this.startingTime) / 1000;
		processingTime *= total/finished - 1;
		percentage = 100 * (finished/total);
		minutes = (int) (processingTime / 60.0d);
		seconds = (int) (processingTime % 60.0d);
	}
	
	/** Get the processing time in seconds. */
	public int getSeconds(){
		return this.seconds;
	}
	/** Get a string summarising the processing/remaining time in minutes and seconds. */
	public String toStringMinutesSeconds(){
		return getMinutes() + " minutes " + getSeconds() + " seconds remaining - "+getPercentage()+"% done";
	}

	/**
	 * get percentage
	 * @return percentage
	 */
	public String getPercentage()
	{
		return Rounding.toString(percentage, 1);
	}
	
	public static String longToText(long timems) {
		int days = 0;
		int hours = 0;
		int mins = 0;
		int secs = 0;
		while (timems>86400000) {
			timems=timems-86400000;
			days++;
		}
		while (timems>3600000) {
			timems=timems-3600000;
			hours++;
		}
		while (timems>60000) {
			timems=timems-60000;
			mins++;
		}
		while (timems>1000) {
			timems=timems-1000;
			secs++;
		}
		return days+" days, "+hours+" hours, "+mins+" minutes, "+secs+" seconds and "+timems+" milliseconds";
	}
}
