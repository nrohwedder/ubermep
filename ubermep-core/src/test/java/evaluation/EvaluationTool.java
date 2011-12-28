package evaluation;

import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: nrohwedder
 * Date: 30.11.11
 * Time: 18:32
 * To change this template use File | Settings | File Templates.
 */
public class EvaluationTool {
	private static final Logger logger = LoggerFactory.getLogger(EvaluationTool.class);
	public static final String KEY_UBERLAY = "Uberlay";
	public static final String KEY_UBERMEP_UNRELIABLE_UNICAST = "Ubermep.UnreliableUnicast";
	public static final String KEY_UBERMEP_RELIABLE_UNICAST = "Ubermep.ReliableUnicast";
	public static final String KEY_UBERMEP_SINGLE_REQUEST_SINGLE_RESPONSE = "Ubermep.SingleRequestSingleResponse";
	private static final String directory = "evaluation/";

	public static void createFile(String filename, int total_runs, EVALUATION_TYPE type) throws IOException {
		String newFileName = formatOutput(directory + filename, total_runs);
		String header = buildHeader(type);

		FileWriter fileOutputStream = new FileWriter(newFileName, false);
		for (int i = 0; i < header.length(); i++) {
			fileOutputStream.write((byte) header.charAt(i));
		}
		fileOutputStream.write("\n");
		fileOutputStream.close();
	}

	@SuppressWarnings("unchecked")
	public static void appendCurrentTimeMapToFile(String filename, Multimap<Integer, TimeInterval> intervalMap, int runs) throws IOException {

		String[][] values = new String[intervalMap.keySet().size()][runs];
		int i = 0;
		for (int key : intervalMap.keySet()) {
			int j = 0;
			Collection<TimeInterval> intervals = intervalMap.get(key);
			for (TimeInterval interval : intervals) {
				values[i][j] = formatOutput("{}", interval);
				j++;
			}
			i++;
		}

		FileWriter fileOutputStream = new FileWriter(directory + filename, true);

		String out	= "";
		for (int j = 0; j < runs; j++) {
			for (i = 0; i < intervalMap.keySet().size(); i++) {
				out += values[i][j] + (i != intervalMap.keySet().size()-1 ? "\t" : "");
			}
			out += "\n";
		}
		fileOutputStream.write(out);
		fileOutputStream.write("\n");
		fileOutputStream.close();
		logger.info("File written!");
	}

	@SuppressWarnings("unchecked")
	public static void appendCurrentTimeMapToFile(String filename, String line) throws IOException {
		FileWriter fileOutputStream = new FileWriter(directory + filename, true);
		for (int i = 0; i < line.length(); i++) {
			fileOutputStream.write((byte) line.charAt(i));
		}
		fileOutputStream.write("\n");
		fileOutputStream.close();
		logger.info("File written!");
	}

	public static String createLineForAppendToFileForPayloadToOverhead(Map<Integer, TimeInterval> intervalMap) {
		String line = "";
		for (int size : createKeyArrayForPayloadToOverhead()) {
			TimeInterval interval = intervalMap.get(size);
			line += formatOutput("{}", interval);
			if (size != createKeyArrayForPayloadToOverhead()[createKeyArrayForPayloadToOverhead().length - 1]) {
				line += "\t";
			}
		}
		return line;
	}

	public static String createLineForAppendToFileForUbermepToUberlay(Map<String, TimeInterval> intervalMap) {
		String line = "";
		for (String key : createKeyArrayForUbermepToUberlay()) {
			TimeInterval interval = intervalMap.get(key);
			line += formatOutput("{}", interval);
			if (!key.equals(createKeyArrayForUbermepToUberlay()[createKeyArrayForUbermepToUberlay().length - 1])) {
				line += "\t";
			}
		}
		return line;
	}

	public static String createLineForAppendToFileForSingleHopVsMultiHop(int key, TimeInterval interval) {
		String line = "";
		int[] keyArray = createKeyArrayForSingleHopVsMultiHop();
		//for (int key : keyArray) {
		//TimeInterval interval = intervalMap.get(key);
		line += formatOutput("{}", interval);
		if (!(key == (keyArray[keyArray.length - 1]))) {
			line += "\t";
		}
		//}
		return line;
	}

	public static String formatOutput(String format, Object... varargs) {
		return MessageFormatter.arrayFormat(format, varargs);
	}

	public static String buildHeader(EVALUATION_TYPE type) {
		String header = "";
		switch (type) {
			case PAYLOAD_TO_OVERHEAD:
				for (int size : createKeyArrayForPayloadToOverhead()) {
					header += formatOutput("{}", size);
					if (size != createKeyArrayForPayloadToOverhead()[createKeyArrayForPayloadToOverhead().length - 1]) {
						header += "\t";
					}
				}
				return header;
			case UBERMEP_TO_UBERLAY:
				for (String key : createKeyArrayForUbermepToUberlay()) {
					header += formatOutput("{}", key);
					if (!key.equals(createKeyArrayForUbermepToUberlay()[createKeyArrayForUbermepToUberlay().length - 1])) {
						header += "\t";
					}
				}
				return header;
			case SINGLEHOP_VS_MULTIHOP:
				for (int size : createKeyArrayForSingleHopVsMultiHop()) {
					header += formatOutput("{}", size);
					if (size != createKeyArrayForSingleHopVsMultiHop()[createKeyArrayForSingleHopVsMultiHop().length - 1]) {
						header += "\t";
					}
				}
				return header;
		}
		return header;
	}

	public static int[] createKeyArrayForPayloadToOverhead() {
		int[] keyArray = new int[7];

		keyArray[0] = 1;
		keyArray[1] = 10;
		keyArray[2] = 100;
		keyArray[3] = 1000;
		keyArray[4] = 10000;
		keyArray[5] = 100000;
		keyArray[6] = 1000000;
		//keyArray[0] = 10000000;
		//keyArray[0] = 100000000;

		return keyArray;
	}

	public static String[] createKeyArrayForUbermepToUberlay() {
		String[] keyArray = new String[4];

		keyArray[0] = KEY_UBERLAY;
		keyArray[1] = KEY_UBERMEP_UNRELIABLE_UNICAST;
		keyArray[2] = KEY_UBERMEP_RELIABLE_UNICAST;
		keyArray[3] = KEY_UBERMEP_SINGLE_REQUEST_SINGLE_RESPONSE;
		return keyArray;
	}

	public static int[] createKeyArrayForSingleHopVsMultiHop() {
		int[] keyArray = new int[5];
		keyArray[0] = 1;
		keyArray[1] = 2;
		keyArray[2] = 3;
		keyArray[3] = 4;
		keyArray[4] = 5;
		//keyArray[5] = 6;
		//keyArray[6] = 7;
		//keyArray[7] = 8;
		//keyArray[8] = 9;
		//keyArray[9] = 10;

		return keyArray;
	}

	public static class TimeInterval {
		private final long startTimestamp;
		private long stopTimestamp;
		boolean stoppageTimestampSet = false;

		public TimeInterval(long startTimestamp) {
			this.startTimestamp = startTimestamp;
		}

		public long getStartTimestamp() {
			return startTimestamp;
		}

		public long getStopTimestamp() {
			return stopTimestamp;
		}

		public synchronized void setStopTimestamp(long stopTimestamp) {
			stoppageTimestampSet = true;
			this.stopTimestamp = stopTimestamp;
			notifyAll();
		}

		public synchronized boolean isDone() {
			return stoppageTimestampSet;
		}

		@Override
		public String toString() {
			if (!stoppageTimestampSet) {
				return "Not valid";
			}
			return stopTimestamp - startTimestamp + "";
		}
	}

	//Temp
	public static class SingleRequestSingleResponseTimeInterval extends TimeInterval{
		private long handlerStoppageTime;
		public SingleRequestSingleResponseTimeInterval(long startTimestamp) {
			super(startTimestamp);
		}

		public void setHandlerStoppageTime(long handlerStoppageTime) {
			this.handlerStoppageTime = handlerStoppageTime;
		}

		public long getHandlerStoppageTime() {
			return handlerStoppageTime - super.getStartTimestamp();
		}

		@Override
		public String toString() {
			if (!stoppageTimestampSet) {
				return "Not valid";
			}
			return (getStopTimestamp() - getStartTimestamp()) + ";" + (handlerStoppageTime - getStartTimestamp());
		}

	}

	public static enum EVALUATION_TYPE {
		PAYLOAD_TO_OVERHEAD, UBERMEP_TO_UBERLAY, SINGLEHOP_VS_MULTIHOP
	}
}
