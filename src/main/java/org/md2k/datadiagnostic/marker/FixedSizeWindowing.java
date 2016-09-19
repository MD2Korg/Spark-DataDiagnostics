package org.md2k.datadiagnostic.marker;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.md2k.datadiagnostic.util.*;
import org.md2k.datadiagnostic.configurations.DDT_PARAMETERS;
import org.md2k.datadiagnostic.signalquality.algorithms.*;
import org.md2k.datadiagnostic.struct.*;

/**
 * This will create fixed equal size windows of time-series data
 * @author Nasir Ali
 *
 */
public class FixedSizeWindowing {

	
	public List<DataPointQuality> windows;
	public List<DataPointQuality> blankWindows;
	
	public FixedSizeWindowing(){
		windows = new ArrayList<DataPointQuality>();
		blankWindows = new ArrayList<DataPointQuality>();
	}
	
	/**
	 * Windowing function for DataPoint arrays
	 *
	 * @param data
	 *            Input data array
	 * @param size
	 *            Time window size in milliseconds
	 * @return ArrayList of data split by size
	 */
	public void createWindows(List<DataPoints> data, long size) {
		long startTime, endTime;
		List<DataPoints> tempArray = new ArrayList<DataPoints>();
		startTime = data.get(0).getTimestamp();
		endTime = data.get(0).getTimestamp() + size;

		List<Integer> temp1 = new ArrayList<Integer>();

		for (int i = 0; i < data.size(); i++) {

			if (data.get(i).getTimestamp() >= startTime && data.get(i).getTimestamp() < endTime) {
				tempArray.add(new DataPoints(data.get(i).getTimestamp(), data.get(i).getValue()));
				temp1.add((int) data.get(i).getValue());
				if (i == data.size() - 1) {
					windows.add(new DataPointQuality(tempArray, 999));
				}
			} else {
				windows.add(new DataPointQuality(tempArray, 999));
				startTime = data.get(i).getTimestamp();
				endTime = data.get(i).getTimestamp() + size;
				tempArray.clear();
				tempArray.add(new DataPoints(data.get(i).getTimestamp(), data.get(i).getValue()));
				if (i == data.size() - 1) {
					windows.add(new DataPointQuality(tempArray, 999));
				}
				temp1.clear();
			}
		}
	}
	
	/**
	 * This method will create n number of blank windows for 24 hours period.
	 * @param sensorData 
	 *
	 * @param data
	 *            Input data array
	 * @param size
	 *            Time window size in milliseconds
	 * @return ArrayList of data split by size
	 */
	public void blankWindows(List<DataPoints> sensorRawData, long startTime, long endTime, long size) {
		List<DataPoints> tempArray = new ArrayList<DataPoints>();
		
		long totalMinutes = ((endTime - startTime)/1000)/60;
		Math.round(totalMinutes);
		for(int i=0;i<totalMinutes;i++){
			long windowStartTime=startTime;
			long windowEndTime = startTime+size;
			
			for (int j = 0; j < sensorRawData.size(); j++) {
				if(sensorRawData.get(j).getTimestamp()>=windowStartTime && sensorRawData.get(j).getTimestamp()<=windowEndTime){
					tempArray.add(new DataPoints(sensorRawData.get(j).getTimestamp(), sensorRawData.get(j).getValue()));
				}
			}
			//tempArray is empty then add empty start and end time
			if(tempArray.isEmpty()){
				tempArray.add(new DataPoints(windowStartTime, startTime));
			}
			
			blankWindows.add(new DataPointQuality(tempArray, 999));
		}
	}

	/**
	 * Creates larger timestamp windows by merging small consecutive windows
	 * 
	 * @param windows timestamp windows 
	 * @param size Time difference between two windows to merge. For example, merge two windows if they are are 1 minute (size=60000) apart.
	 * @return ArrayList<DataPoints> merged windows in larger windows
	 */
	public ArrayList<DataPoints> mergeDataPointsWindows(List<DataPoints> windows, long size) {
		List<Long> temp = new ArrayList<Long>();
		ArrayList<DataPoints> mergedWindows = new ArrayList<DataPoints>();

		if (windows.size() == 1) {
			mergedWindows.add(new DataPoints(windows.get(0).getTimestamp(), windows.get(0).getEndTimestamp()));
		}
		for (int i = 0; i < windows.size() - 1; i++) {

			if (windows.get(i + 1).getTimestamp() - windows.get(i).getEndTimestamp() > size) {

				if (temp.size() == 0) {
					mergedWindows.add(new DataPoints(windows.get(i).getTimestamp(), windows.get(i).getEndTimestamp()));
				} else {
					mergedWindows.add(new DataPoints(temp.get(0), windows.get(i).getEndTimestamp()));
				}

				temp.clear();
			} else {
				temp.add(windows.get(i).getTimestamp());
				if (i == windows.size() - 2) {
					mergedWindows.add(new DataPoints(temp.get(0), windows.get(i + 1).getEndTimestamp()));
				}
			}
		}
		return mergedWindows;

	}
	
}
