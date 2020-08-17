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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * Converts binary tracker data to a few known formats.
 */
public class TrackerConverter {

	/**
	 * The main entry for the converter.
	 * @param args optional input file path as the one and only argument
	 * @throws Exception error handling limited to a minimum, all others are thrown out
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("Caution! The CSV file will be generated accordingly to the current locale settings = " + Locale.getDefault(Locale.Category.FORMAT));
		System.out.println("Pretty stupid, but depending on the language version of your Excel, it expects either a dot or a comma as a decimal separator.");
		
		File inputFile;
		
		// open the input file
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setVisible(true);
		
		int result;
		if (args.length == 1) {
			inputFile = new File(args[0]);
		} else {
			result = fileChooser.showOpenDialog(null);
			if (result == JFileChooser.APPROVE_OPTION) {
				inputFile = fileChooser.getSelectedFile();
				System.out.println("Input file = " + inputFile);
			} else {
				return;
			}
		}

		// interpret the header
        FileInputStream fis = new FileInputStream(inputFile);

        byte[] buffer = new byte[FileItem.HEADER.length];
        if (fis.read(buffer) != FileItem.HEADER.length) {
        	fis.close();
            throw new IOException("Unknown file format (1).");
        }

        if (!Arrays.equals(buffer, FileItem.HEADER)) {
        	fis.close();
            throw new IOException("Unknown file format (2).");
        }

        buffer = new byte[2];
        fis.read(buffer);
        int version = BitUtility.getShort(buffer, 0);
        if (version != FileItem.VERSION) {
        	fis.close();
            throw new IOException("Unsupported version no. " + version);
        }
        
        // read sport activity data
        SportActivityData data = new SportActivityData();

        short id;
        do {
            buffer = FileItem.readField(fis);
            int offset = 0;
            id = BitUtility.getShort(buffer, offset);
            offset += 2;

            switch (id) {
                case 0x1001: // start RTC
                    data.setStartTimestampRtc(BitUtility.getLong(buffer, offset));
                    break;

                case 0x1002: // stop RTC
                    data.setStopTimestampRtc(BitUtility.getLong(buffer, offset));
                    break;

                case 0x1003: // start ns
                    data.setStartNanoseconds(BitUtility.getLong(buffer, offset));
                    break;

                case 0x1004: // stop ns
                    data.setStopNanoseconds(BitUtility.getLong(buffer, offset));
                    break;

                case 0x1011: // avg heart rate (float)
                    data.setAvgHeartRate(BitUtility.getFloat(buffer, offset));
                    break;

                case 0x1012: // max heart rate (int)
                    data.setMaxHeartRate(BitUtility.getInt(buffer, offset));
                    break;

                case 0x1013: // total steps (int)
                    data.setTotalSteps(BitUtility.getInt(buffer, offset));
                    break;

                case 0x1014: // avg step rate (float)
                    data.setAvgStepRate(BitUtility.getFloat(buffer, offset));
                    break;

                case 0x1015: // total ascent (float)
                    data.setTotalAscent(BitUtility.getFloat(buffer, offset));
                    break;

                case 0x1016: // total descent (float)
                    data.setTotalDescent(BitUtility.getFloat(buffer, offset));
                    break;

                case 0x1017: // avg speed (float)
                    data.setAvgSpeed(BitUtility.getFloat(buffer, offset));
                    break;
                    
                case 0x2011: // heart rate
                	data.addEvent(new HeartRateEvent(
                			BitUtility.getLong(buffer, offset),
                			BitUtility.getInt(buffer, offset + 8),
                			BitUtility.getInt(buffer, offset + 8 + 4)));
                	break;
                	
                case 0x2021: // steps
                	data.addEvent(new StepsEvent(
                			BitUtility.getLong(buffer, offset),
                			BitUtility.getInt(buffer, offset + 8),
                			BitUtility.getInt(buffer, offset + 8 + 4)));
                	break;
                	
                case 0x2031: // air pressure
                	data.addEvent(new AirPressureEvent(
                			BitUtility.getLong(buffer, offset),
                			BitUtility.getFloat(buffer, offset + 8),
                			BitUtility.getInt(buffer, offset + 8 + 4)));
                	break;
                	
                case 0x2041: // geo
                	data.addEvent(new GeoEvent(
                			BitUtility.getLong(buffer, offset), //ts
                			BitUtility.getLong(buffer, offset + 8), //ticks
                			BitUtility.getLong(buffer, offset + 16), //time
                			BitUtility.getDouble(buffer, offset + 24), //latitude
                			BitUtility.getDouble(buffer, offset + 32), //longitude
                			BitUtility.getFloat(buffer, offset + 40), //accuracy
                			BitUtility.getDouble(buffer, offset + 44), //altitude
                			BitUtility.getFloat(buffer, offset + 52), //bearing
                			BitUtility.getFloat(buffer, offset + 56), //speed
                			BitUtility.getInt(buffer, offset + 60))); //sensorAccuracy
                	break;
            }

        } while (id != (short)0xffff);

        fis.close();
        
        // show some statistics
        
        String startDateFormatted = new Date(data.getStartTimestampRtc()).toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL));
        System.out.println("Start " + data.getStartTimestampRtc() + " = " + startDateFormatted);
        String stopDateFormatted = new Date(data.getStopTimestampRtc()).toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL));
        System.out.println("Stop  " + data.getStopTimestampRtc() + " = " + stopDateFormatted);
        long totalSeconds = (data.getStopTimestampRtc() - data.getStartTimestampRtc()) / 1000;
        int minutes = (int)(totalSeconds / 60);
        int seconds = (int)(totalSeconds - minutes * 60);
        
        System.out.println("Duration " + String.format("%02d:%02d", minutes, seconds));
        System.out.println("Start ticks " + data.getStartNanoseconds());
        System.out.println("Stop  ticks " + data.getStopNanoseconds());
        System.out.println();
        System.out.println("Heart Rate");
        System.out.println("  - Avg " + data.getAvgHeartRate() + " bpm (stored), " + data.getCalculatedAvgHeartRate() + " bpm (calculated)");
        System.out.println("  - Max " + data.getMaxHeartRate() + " bpm (stored), " + data.getCalculatedMaxHeartRate() + " bmp (calculated)");
        System.out.println("  - Events recorded: " + data.getAccurateHeartRateEvents() + " accurate, " + data.getInaccurateHeartRateEvents() + " inaccurate, " + data.getOutOfScopeHeartRateEvents() + " out of scope");
        System.out.println();
        System.out.println("Steps");
        System.out.println("  - Count " + data.getTotalSteps() + " (stored), " + data.getCalculatedTotalSteps() + " (calculated)");
        System.out.println("  - Avg " + data.getAvgStepRate() + " steps/min (stored), " + data.getCalculatedAvgStepsPerMinute() + " steps/min (calculated)");
        System.out.println("  - Max " + data.getCalculatedMaxStepsPerMinute() + " steps/min (calculated)");
        System.out.println("  - Events recorded: " + data.getAccurateCountStepsEvents() + " accurate, " + data.getInaccurateStepsEvents() + " inaccurate, " + data.getOutOfScopeStepsEvents() + " out of scope");
        System.out.println();
        System.out.println("Geo");
        System.out.println("  - Avg " + data.getAvgSpeed() + " m/s = " + (data.getAvgSpeed() * 3.6f) + " km/h (stored), " + data.getCalculatedAvgSpeed() + " m/s = " + (data.getCalculatedAvgSpeed() * 3.6f) + " km/h (calculated)");
        System.out.println("  - Max " + data.getCalculatedMaxSpeed() + " m/s = " + (data.getCalculatedMaxSpeed() * 3.6f) + " km/h (calculated)");
        System.out.println("  - Avg +/-" + data.getCalculatedAvgAccuracy() + " m (calculated)");
        System.out.println("  - Best accuracy +/-" + data.getCalculatedBestAccuracy() + " m (calculated)");
        System.out.println("  - Worst accuracy +/-" + data.getCalculatedWorstAccuracy() + " m (calculated)");
        System.out.println("  - Total ascent " + data.getTotalAscent() + " m (stored)");
        System.out.println("  - Total descent " + data.getTotalDescent() + " m (stored)");
        System.out.println("  - Events recorded: " + data.getValidGeoEvents() + " valid, " + data.getOutOfScopeGeoEvents() + " out of scope");
        System.out.println();
        System.out.println("* stored = as in the file generated by the WearOS app; calculated = here based on all events");

        // generate KML file
        
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setFileFilter(new FileFilter() {
			
			@Override
			public String getDescription() {
				return "Google Earth (*.kml)";
			}
			
			@Override
			public boolean accept(File f) {
				String name = f.getName();
				int index = name.lastIndexOf('.'); 
				return (index >= 0) && (name.length() > index + 1) && (name.substring(index + 1).equalsIgnoreCase("kml"));
			}
		});
        
        fileChooser.setSelectedFile(new File(inputFile.getParentFile(), inputFile.getName() + ".kml"));
        
        result = fileChooser.showSaveDialog(null);
        
        if (result == JFileChooser.APPROVE_OPTION) {
        	
        	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileChooser.getSelectedFile()), Charset.forName("UTF-8")));
        	
        	bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        	bw.newLine();
        	bw.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">");
        	bw.newLine();
        	bw.write("<Document>");
        	bw.newLine();
        	bw.write("<name>TestDocument</name>");
        	bw.newLine();
        	bw.write("<Style id=\"s_ylw-pushpin_hl\">");
        	bw.newLine();
        	bw.write("<IconStyle>");
        	bw.newLine();
        	bw.write("<scale>1.3</scale>");
        	bw.newLine();
        	bw.write("<Icon>");
        	bw.newLine();
        	bw.write("<href>http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png</href>");
        	bw.newLine();
        	bw.write("</Icon>");
        	bw.newLine();
        	bw.write("<hotSpot x=\"20\" y=\"2\" xunits=\"pixels\" yunits=\"pixels\"/>");
        	bw.newLine();
        	bw.write("</IconStyle>");
        	bw.newLine();
        	bw.write("</Style>");
        	bw.newLine();
        	bw.write("<Style id=\"s_ylw-pushpin\">");
        	bw.newLine();
        	bw.write("<IconStyle>");
        	bw.newLine();
        	bw.write("<scale>1.1</scale>");
        	bw.newLine();
        	bw.write("<Icon>");
        	bw.newLine();
        	bw.write("<href>http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png</href>");
        	bw.newLine();
        	bw.write("</Icon>");
        	bw.newLine();
        	bw.write("<hotSpot x=\"20\" y=\"2\" xunits=\"pixels\" yunits=\"pixels\"/>");
        	bw.newLine();
        	bw.write("</IconStyle>");
        	bw.newLine();
        	bw.write("</Style>");
        	bw.newLine();
        	bw.write("<StyleMap id=\"m_ylw-pushpin\">");
        	bw.newLine();
        	bw.write("<Pair>");
        	bw.newLine();
        	bw.write("<key>normal</key>");
        	bw.newLine();
        	bw.write("<styleUrl>#s_ylw-pushpin</styleUrl>");
        	bw.newLine();
        	bw.write("</Pair>");
        	bw.newLine();
        	bw.write("<Pair>");
        	bw.newLine();
        	bw.write("<key>highlight</key>");
        	bw.newLine();
        	bw.write("<styleUrl>#s_ylw-pushpin_hl</styleUrl>");
        	bw.newLine();
        	bw.write("</Pair>");
        	bw.newLine();
        	bw.write("</StyleMap>");
        	bw.newLine();
        	bw.write("<Placemark>");
        	bw.newLine();
        	bw.write("<name>Test Path</name>");
        	bw.newLine();
        	bw.write("<styleUrl>#m_ylw-pushpin</styleUrl>");
        	bw.newLine();
        	bw.write("<LineString>");
        	bw.newLine();
        	bw.write("<tessellate>1</tessellate>");
        	bw.newLine();
        	bw.write("<coordinates>");
        	bw.newLine();
        	
        	for (GeoEvent geoData : data.getGeoEvents()) {
        		bw.write(String.format(Locale.US, "%.9f,%.9f,%d ", geoData.getLongitude(), geoData.getLatitude(), 0));
        	}
        	
        	bw.write("</coordinates>");
        	bw.newLine();
        	bw.write("</LineString>");
        	bw.newLine();
        	bw.write("</Placemark>");
        	bw.newLine();
        	bw.write("</Document>");
        	bw.newLine();
        	bw.write("</kml>");
        	bw.newLine();
        	
        	bw.close();
        }
        
        // save all events chronologically to a CSV file
        
        fileChooser.setSelectedFile(new File(inputFile.getParentFile(), inputFile.getName() + ".csv"));
        result = fileChooser.showSaveDialog(null);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            // sort all events
            List<SensorEvent> allEvents = new ArrayList<>();
            allEvents.addAll(data.getHeartRateEvents());
            allEvents.addAll(data.getStepsEvents());
            allEvents.addAll(data.getAirPressureEvents());
            allEvents.addAll(data.getGeoEvents());
            allEvents.sort(new Comparator<SensorEvent>() {
            	@Override
            	public int compare(SensorEvent o1, SensorEvent o2) {
            		return o1.getTsNs() < o2.getTsNs() ? -1 : o1.getTsNs() > o2.getTsNs() ? 1 : 0;
            	}
    		});

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileChooser.getSelectedFile()), Charset.forName("UTF-8")));
        	
        	bw.write("\"Timestamp (ms)\";\"Sensor Accuracy\";\"Heart Rate (bpm)\";\"Steps\";\"Air Pressure (mbar)\";\"GNSS Time (ms)\";\"Latitude\";\"Longitude\";\"Position Accuracy (m)\";\"GNSS Altitude (m)\";\"GNSS Bearing\";\"GNSS Speed (m/s)\"");
        	bw.newLine();
        	
        	for (SensorEvent event : allEvents) {
        		String ts = String.valueOf((event.getTsNs() - data.getStartNanoseconds()) / 1000 / 1000);
        		
        		if (event instanceof HeartRateEvent) {
        			HeartRateEvent heartRateEvent = (HeartRateEvent)event;
        			bw.write(ts + ";" + heartRateEvent.getAccuracy() + ";" + heartRateEvent.getRate() + ";;;;;;;;;");
        		} else if (event instanceof StepsEvent) {
        			StepsEvent stepsEvent = (StepsEvent)event;
        			bw.write(ts + ";" + stepsEvent.getAccuracy() + ";;" + stepsEvent.getSteps() + ";;;;;;;;");
        		} else if (event instanceof AirPressureEvent) {
        			AirPressureEvent airPressureEvent = (AirPressureEvent)event;
        			bw.write(ts + ";" + airPressureEvent.getAccuracy() + ";;;" + String.format("%.2f", airPressureEvent.getPressure()) + ";;;;;;;");
        		} else if (event instanceof GeoEvent) {
        			GeoEvent geoEvent = (GeoEvent)event;
        			bw.write(ts + ";" + geoEvent.getAccuracy() + ";;;;"
        					+ geoEvent.getFixRtcTime() + ";"
        					+ String.format("%.9f", geoEvent.getLatitude()) + ";"
        					+ String.format("%.9f", geoEvent.getLongitude()) + ";"
        					+ String.format("%.2f", geoEvent.getLateralAccuracy()) + ";"
        					+ String.format("%.2f", geoEvent.getAltitude()) + ";"
        					+ String.format("%.2f", geoEvent.getBearing()) + ";"
        					+ String.format("%.2f", geoEvent.getSpeed()));
        		} else {
        			System.err.println("Unsupported sensor event type " + event.getClass().getSimpleName());
        			continue;
        		}
        		
        		bw.newLine();
        	}
        	
        	bw.close();
        }
	}

}
