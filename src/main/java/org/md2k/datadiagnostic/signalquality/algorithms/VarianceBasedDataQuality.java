package org.md2k.datadiagnostic.signalquality.algorithms;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.lang.model.element.VariableElement;

import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.md2k.datadiagnostic.configurations.DATA_QUALITY;
import org.md2k.datadiagnostic.configurations.DDT_PARAMETERS;
import org.md2k.datadiagnostic.configurations.METADATA;
import org.md2k.datadiagnostic.struct.DataPoints;
import org.md2k.datadiagnostic.util.Statistics;
import org.md2k.datadiagnostic.util.Util;

/*
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class VarianceBasedDataQuality  {

    private static final double RIP_VARIANCE_THRESHOLD = 1000; //0.00003 for microsoft band
    
    public int currentQuality(List<DataPoints> timestampsAndValues, long expectedSamples) {
    	List<Double> normalValues = new ArrayList<Double>();
    	List<Double> values = new ArrayList<Double>();
    	
    	double expectedSample = timestampsAndValues.size()/expectedSamples;
    	if(expectedSample<DDT_PARAMETERS.MINIMUM_ACCEPTABLE_PACKET_LOSS){
    		//return METADATA.DATA_LOST;
    	}
    	if (timestampsAndValues.size() == 0) {
            return DATA_QUALITY.SENSOR_OFF;
        }
    	
    	for(int i=0;i<timestampsAndValues.size();i++){
    		values.add(timestampsAndValues.get(i).getValue());
    	}

    	Statistics statistics = new Statistics(values);
    	normalValues.addAll(statistics.StatisticalOutLierAnalysis());

    	//Mark window as jerks if outliers are 70% of a window.
    	
    	Statistics statistics2 = new Statistics(normalValues);
    	double variance = statistics2.getVariance();
       	
    	if(variance<RIP_VARIANCE_THRESHOLD){
    		return METADATA.SENSOR_OFF_BODY;
		}
    	return METADATA.SENSOR_ON_BODY;
    	
    }

    //Original variance data quality algo
    public int currentQuality2(List<Integer> rawData, List<DataPoints> tempArray) {
       int[] samples = rawData.stream().mapToInt(i -> i).toArray();
        if (samples.length == 0) {
            return DATA_QUALITY.SENSOR_OFF;
        }
        double K = samples[0];
        long n = 0;
        double sum = 0;
        double sum_sqr = 0;
        int x;

        double max = 0;
        double min = 10000;

        for (int i = 0; i < samples.length; i++) {
            x = samples[i];
            n++;
            sum += (x - K);
            sum_sqr += (x - K) * (x - K);

            if (x > max)
                max = x;
            if (x < min)
                min = x;
        }
        double variance = (sum_sqr - (sum * sum) / n) / n;
        //Log.d("DATA_QUALITY", "RIP: VARIANCE: " + variance + " (" + min + "," + max + ")");
        if (variance < RIP_VARIANCE_THRESHOLD) {
            return DATA_QUALITY.SENSOR_OFF_BODY;
        }
    	System.out.println(variance);
        return DATA_QUALITY.GOOD;
    }}
