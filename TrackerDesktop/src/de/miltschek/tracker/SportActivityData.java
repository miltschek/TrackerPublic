/*
 *  MIT License
 *
 *  Copyright (c) 2020 miltschek
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package de.miltschek.tracker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Container of the sport activity data.
 */
public class SportActivityData {
    private long startTimestampRtc;
    private long stopTimestampRtc;
    private long startNanoseconds;
    private long stopNanoseconds;
    private float avgHeartRate;
    private int maxHeartRate;
    private int totalSteps;
    private float avgStepRate;
    private float totalAscent;
    private float totalDescent;
    private float avgSpeed;
    
    // for additional statistic calculation (heart rate)
    float calculatedAvgHeartRate = 0, calculatedMaxHeartRate = 0;
    int accurateHeartRateEvents = 0, inaccurateHeartRateEvents = 0, outOfScopeHeartRateEvents = 0;
    
    // for additional statistic calculation (steps)
	long calculatedFirstStepsTs = 0;
    long calculatedLastStepsTs = 0;
    int calculatedInitialSteps = 0;
    int calculatedLastSteps = 0;
    int calculatedFinalSteps = 0;
    float calculatedMaxStepsPerMinute = 0;
    int accurateCountStepsEvents = 0, inaccurateStepsEvents = 0, outOfScopeStepsEvents = 0;
    
    float calculatedAvgSpeed = 0, calculatedMaxSpeed = 0;
    int validGeoEvents = 0, outOfScopeGeoEvents = 0;
    float calculatedAvgAccuracy = 0, calculatedBestAccuracy = 999f, calculatedWorstAccuracy = 0;

    // individual events
    private List<HeartRateEvent> heartRateEvents = new ArrayList<HeartRateEvent>();
    private List<StepsEvent> stepEvents = new ArrayList<StepsEvent>();
    private List<GeoEvent> geoEvents = new ArrayList<GeoEvent>();
    private List<AirPressureEvent> airPressureEvents = new ArrayList<AirPressureEvent>();
    
    /**
     * Returns the real time clock timestamp of the beginning of the sport activity.
     * @return beginning of the sport activity timestamp (ms since Jan, 1st 1970)
     */
	public long getStartTimestampRtc() {
		return startTimestampRtc;
	}
	
	/**
	 * Sets the real time clock timestamp of the beginning of the sport activity.
	 * @param startTimestampRtc beginning of the sport activity timestamp (ms since Jan, 1st 1970)
	 */
	public void setStartTimestampRtc(long startTimestampRtc) {
		this.startTimestampRtc = startTimestampRtc;
	}
	
	/**
	 * Returns the real time clock timestamp of the end of the sport activity.
	 * @return end of the sport activity timestamp (ms since Jan, 1st 1970)
	 */
	public long getStopTimestampRtc() {
		return stopTimestampRtc;
	}
	
	/**
	 * Sets the real time clock timestamp of the end of the sport activity.
	 * @param stopTimestampRtc end of the sport activity timestamp (ms since Jan, 1st 1970)
	 */
	public void setStopTimestampRtc(long stopTimestampRtc) {
		this.stopTimestampRtc = stopTimestampRtc;
	}
	
	/**
	 * Returns an abstract timestamp of the beginning of the sport activity.
	 * @return beginning of the sport activity (ns)
	 */
	public long getStartNanoseconds() {
		return startNanoseconds;
	}

	/**
	 * Sets the abstract timestamp of the beginning of the sport activity.
	 * @param startNanoseconds beginning of the sport activity (ns)
	 */
	public void setStartNanoseconds(long startNanoseconds) {
		this.startNanoseconds = startNanoseconds;
	}
	
	/**
	 * Returns an abstract timestamp of the end of the sport activity.
	 * @return end of the sport activity (ns)
	 */
	public long getStopNanoseconds() {
		return stopNanoseconds;
	}
	
	/**
	 * Sets the abstract timestamp of the end of the sport activity.
	 * @param stopNanoseconds end of the sport activity (ns)
	 */
	public void setStopNanoseconds(long stopNanoseconds) {
		this.stopNanoseconds = stopNanoseconds;
	}
	
	/**
	 * Returns the stored average heart rate.
	 * @return average heart rate (bpm)
	 */
	public float getAvgHeartRate() {
		return avgHeartRate;
	}
	
	/**
	 * Sets the stored average heart rate.
	 * @param avgHeartRate average heart rate (bpm)
	 */
	public void setAvgHeartRate(float avgHeartRate) {
		this.avgHeartRate = avgHeartRate;
	}
	
	/**
	 * Returns the stored maximum heart rate.
	 * @return maximum heart rate (bpm)
	 */
	public int getMaxHeartRate() {
		return maxHeartRate;
	}
	
	/**
	 * Sets the stored maximum heart rate.
	 * @param maxHeartRate maximum heart rate (bpm)
	 */
	public void setMaxHeartRate(int maxHeartRate) {
		this.maxHeartRate = maxHeartRate;
	}
	
	/**
	 * Returns the stored amount of steps in total.
	 * @return total amount of steps
	 */
	public int getTotalSteps() {
		return totalSteps;
	}
	
	/**
	 * Sets the stored amount of steps in total.
	 * @param totalSteps total amount of steps
	 */
	public void setTotalSteps(int totalSteps) {
		this.totalSteps = totalSteps;
	}
	
	/**
	 * Returns the stored average steps frequency.
	 * @return average steps frequency (steps per minute)
	 */
	public float getAvgStepRate() {
		return avgStepRate;
	}
	
	/**
	 * Sets the stored average steps frequency.
	 * @param avgStepRate average steps frequency (steps per minute)
	 */
	public void setAvgStepRate(float avgStepRate) {
		this.avgStepRate = avgStepRate;
	}
	
	/**
	 * Returns the stored total ascent.
	 * @return total ascent (m)
	 */
	public float getTotalAscent() {
		return totalAscent;
	}
	
	/**
	 * Sets the stored total ascent.
	 * @param totalAscent total ascent (m)
	 */
	public void setTotalAscent(float totalAscent) {
		this.totalAscent = totalAscent;
	}
	
	/**
	 * Returns the stored total descent.
	 * @return total descent (m)
	 */
	public float getTotalDescent() {
		return totalDescent;
	}
	
	/**
	 * Sets the stored total descent.
	 * @param totalDescent total descent (m)
	 */
	public void setTotalDescent(float totalDescent) {
		this.totalDescent = totalDescent;
	}
	
	/**
	 * Returns the stored average speed.
	 * @return average speed (m/s)
	 */
	public float getAvgSpeed() {
		return avgSpeed;
	}
	
	/**
	 * Sets the stored average speed.
	 * @param avgSpeed average speed (m/s)
	 */
	public void setAvgSpeed(float avgSpeed) {
		this.avgSpeed = avgSpeed;
	}

	/**
	 * Stores a new heart event and updates the statistics.
	 * @param heartEvent heart event
	 */
    public void addEvent(HeartRateEvent heartEvent) {
    	this.heartRateEvents.add(heartEvent);
    	
    	if (heartEvent.getTsNs() < startNanoseconds || heartEvent.getTsNs() > stopNanoseconds) {
    		outOfScopeHeartRateEvents++;
    	} else if (heartEvent.getAccuracy() >= 1) {
    		calculatedAvgHeartRate = calculatedAvgHeartRate * (accurateHeartRateEvents++ / (float)accurateHeartRateEvents) + heartEvent.getRate() / (float)accurateHeartRateEvents;
        	if (calculatedMaxHeartRate < heartEvent.getRate()) {
        		calculatedMaxHeartRate = heartEvent.getRate();
        	}
        } else {
        	inaccurateHeartRateEvents++;
        }
    }

    /**
     * Stores a new steps event and updates the statistics.
     * @param stepsEvent steps event
     */
    public void addEvent(StepsEvent stepsEvent) {
    	this.stepEvents.add(stepsEvent);
    	
    	if (stepsEvent.getTsNs() < startNanoseconds || stepsEvent.getTsNs() > stopNanoseconds) {
    		outOfScopeStepsEvents++;
    	} else if (stepsEvent.getAccuracy() >= 1) {
	    	if (calculatedLastStepsTs == 0) {
	    		calculatedFirstStepsTs = stepsEvent.getTsNs();
	    		calculatedLastStepsTs = stepsEvent.getTsNs();
	    		calculatedLastSteps = stepsEvent.getSteps();
	    		calculatedInitialSteps = stepsEvent.getSteps();
	        } else {
	        	accurateCountStepsEvents++;
	    		calculatedFinalSteps = stepsEvent.getSteps();
	    		
	    		int deltaSteps = stepsEvent.getSteps() - calculatedLastSteps;
	    		float deltaTimeMinutes = (stepsEvent.getTsNs() - calculatedLastStepsTs) / 1000f / 1000f / 1000f / 60f;
	    		float stepsPerMinute = deltaSteps / deltaTimeMinutes;
	    		
	    		if (calculatedMaxStepsPerMinute < stepsPerMinute) {
	    			calculatedMaxStepsPerMinute = stepsPerMinute;
	    		}
	    		
	    		calculatedLastStepsTs = stepsEvent.getTsNs();
	    		calculatedLastSteps = stepsEvent.getSteps();
	        }
    	} else {
        	inaccurateStepsEvents++;
        }
    }

    /**
     * Stores a new air pressure event and updates the statistics.
     * @param airPressureEvent air pressure event
     */
    public void addEvent(AirPressureEvent airPressureEvent) {
    	this.airPressureEvents.add(airPressureEvent);
    }

    /**
     * Stores a new geo event and updates the statistics.
     * @param geoEvent geo event
     */
    public void addEvent(GeoEvent geoEvent) {
    	this.geoEvents.add(geoEvent);
    	
    	if (geoEvent.getTsNs() < startNanoseconds || geoEvent.getTsNs() > stopNanoseconds) {
    		outOfScopeGeoEvents++;
    	} else {
    		validGeoEvents++;
    		
	    	calculatedAvgSpeed = calculatedAvgSpeed * ((validGeoEvents - 1) / (float)validGeoEvents)
	    			+ geoEvent.getSpeed() / validGeoEvents;
	    	
	    	if (calculatedMaxSpeed < geoEvent.getSpeed()) {
	    		calculatedMaxSpeed = geoEvent.getSpeed();
	    	}
	    	
	    	calculatedAvgAccuracy = calculatedAvgAccuracy * ((validGeoEvents - 1) / (float)validGeoEvents)
	    			+ geoEvent.getAccuracy() / validGeoEvents;
	    	
	    	if (calculatedBestAccuracy > geoEvent.getAccuracy()) {
	    		calculatedBestAccuracy = geoEvent.getAccuracy();
	    	}
	    	
	    	if (calculatedWorstAccuracy < geoEvent.getAccuracy()) {
	    		calculatedWorstAccuracy = geoEvent.getAccuracy();
	    	}
    	}
    }

    /**
     * Returns the calculated average heart rate.
     * @return average heart rate (bpm)
     */
    public float getCalculatedAvgHeartRate() {
		return calculatedAvgHeartRate;
	}
	
    /**
     * Returns the calculated maximum heart rate.
     * @return maximum heart rate (bpm)
     */
    public float getCalculatedMaxHeartRate() {
		return calculatedMaxHeartRate;
	}
	
    /**
     * Returns the calculated average steps frequency.
     * @return average steps frequency (steps per minute)
     */
    public float getCalculatedAvgStepsPerMinute() {
		return (calculatedFinalSteps - calculatedInitialSteps) / ((calculatedLastStepsTs - calculatedFirstStepsTs) / 1000 / 1000 / 1000 / 60f);
	}
	
    /**
     * Returns the calculated maximum steps frequency.
     * @return maximum steps frequency (steps per minute)
     */
    public float getCalculatedMaxStepsPerMinute() {
		return calculatedMaxStepsPerMinute;
	}
	
    /**
     * Returns the calculated average speed.
     * @return average speed (m/s)
     */
    public float getCalculatedAvgSpeed() {
		return calculatedAvgSpeed;
	}
	
    /**
     * Returns the calculated maximum speed.
     * @return maximum speed (m/s)
     */
    public float getCalculatedMaxSpeed() {
		return calculatedMaxSpeed;
	}
	
    /**
     * Returns the calculated average position accuracy.
     * @return average position accuracy (m)
     */
    public float getCalculatedAvgAccuracy() {
		return calculatedAvgAccuracy;
	}
	
    /**
     * Returns the best position accuracy that has been reported.
     * @return best reported position accuracy (m)
     */
    public float getCalculatedBestAccuracy() {
		return calculatedBestAccuracy;
	}
	
    /**
     * Returns the worst position accuracy that has been reported.
     * @return worst reported position accuracy (m)
     */
    public float getCalculatedWorstAccuracy() {
		return calculatedWorstAccuracy;
	}
	
    /**
     * Returns the number of accurate heart rate events.
     * @return number of accurate heart rate events
     */
    public int getAccurateHeartRateEvents() {
		return accurateHeartRateEvents;
	}
	
    /**
     * Returns the number of inaccurate heart rate events.
     * @return number of inaccurate heart rate events
     */
    public int getInaccurateHeartRateEvents() {
		return inaccurateHeartRateEvents;
	}
	
    /**
     * Returns the number of out of scope heart rate events (recorded outside of the sport activity time span).
     * @return number of out of scope heart rate events
     */
    public int getOutOfScopeHeartRateEvents() {
		return outOfScopeHeartRateEvents;
	}
	
    /**
     * Returns the number of accurate steps events.
     * @return number of accurate steps events
     */
    public int getAccurateCountStepsEvents() {
		return accurateCountStepsEvents;
	}
	
    /**
     * Returns the number of inaccurate steps events.
     * @return number of inaccurate steps events
     */
    public int getInaccurateStepsEvents() {
		return inaccurateStepsEvents;
	}
	
    /**
     * Returns the number of out of scope steps events (recorded outside of the sport activity time span).
     * @return number of out of scope steps events
     */
    public int getOutOfScopeStepsEvents() {
		return outOfScopeStepsEvents;
	}
	
    /**
     * Returns the number of out of scope geo events (recorded outside of the sport activity time span).
     * @return number of out of scope geo events
     */
    public int getOutOfScopeGeoEvents() {
		return outOfScopeGeoEvents;
	}
    
    /**
     * Returns the calculated total amount of steps.
     * @return total amount of steps
     */
    public int getCalculatedTotalSteps() {
    	return calculatedFinalSteps - calculatedInitialSteps;
    }
	
    /**
     * Returns the number of valid geo events.
     * @return number of valid geo events
     */
    public int getValidGeoEvents() {
		return validGeoEvents;
	}
    
    /**
     * Returns a collection of all stored heart rate events.
     * @return collection of all stored heart rate events
     */
    public Collection<? extends HeartRateEvent> getHeartRateEvents() {
    	return heartRateEvents;
    }

    /**
     * Returns a collection of all stored steps events.
     * @return collection of all stored steps events
     */
    public Collection<? extends StepsEvent> getStepsEvents() {
    	return stepEvents;
    }

    /**
     * Returns a collection of all stored air pressure events.
     * @return collection of all stored air pressure events
     */
    public Collection<? extends AirPressureEvent> getAirPressureEvents() {
    	return airPressureEvents;
    }

    /**
     * Returns a collection of all stored geo events.
     * @return collection of all stored geo events
     */
    public Collection<? extends GeoEvent> getGeoEvents() {
    	return geoEvents;
    }
}
