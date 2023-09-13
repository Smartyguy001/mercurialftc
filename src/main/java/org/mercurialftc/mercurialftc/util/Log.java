package org.mercurialftc.mercurialftc.util;

import android.os.Environment;
import org.mercurialftc.mercurialftc.scheduler.Scheduler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Log {
	private final long startTime;
	private final String now;

	private final String[] dataLine;
	private final String[] dataHeadings;
	private FileWriter fileWriter;

	public Log(String subsystem, int storedLogs, String... dataHeadings) {
		this.startTime = System.nanoTime();
		this.now = String.valueOf(System.nanoTime() / 1E9);
		this.dataHeadings = new String[dataHeadings.length + 1];
		this.dataLine = new String[this.dataHeadings.length];
		System.arraycopy(dataHeadings, 0, this.dataHeadings, 1, dataHeadings.length);

		if (!Scheduler.isLoggingEnabled()) {
			return;
		}

		String directoryPath = Environment.getExternalStorageDirectory().getPath() + "/FIRST/mercurialftc/logs/" + subsystem;
		File directory = new File(directoryPath);
		directory.mkdirs();

		File outdatedLog = new File(directoryPath + "/" + subsystem + (storedLogs - 1) + ".csv");
		outdatedLog.delete();
		for (int i = storedLogs; i > 0; i--) {
			File oldLog = new File(directoryPath + "/" + subsystem + (i - 1) + ".csv");
			File oldLogDestination = new File(directoryPath + "/" + subsystem + (i) + ".csv");
			oldLog.renameTo(oldLogDestination);
		}
		try {
			fileWriter = new FileWriter(directoryPath + "/" + subsystem + "0.csv", true);
		} catch (IOException e) {
			e.printStackTrace();
		}

		createHeadings();
	}

	public Log(String subsystem, String... dataHeadings) {
		this(subsystem, 6, dataHeadings);
	}

	public void updateLoop(boolean storeTime) {
		if (!Scheduler.isLoggingEnabled()) {
			return;
		}
		double elapsedTime = ((System.nanoTime() - startTime) / 1E9);
		StringBuilder dataWrite = new StringBuilder();
		if (storeTime) {
			dataLine[0] = String.valueOf(elapsedTime);
		}
		for (int i = 0; i < dataLine.length; i++) {
			dataWrite.append(dataLine[i]);
			if (i != dataLine.length - 1) {
				dataWrite.append(",");
			}
		}

		try {
			fileWriter.write(dataWrite + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void logData(int dataHeadingArrayPosition, Object data) {
		if (!Scheduler.isLoggingEnabled()) {
			return;
		}
		dataLine[dataHeadingArrayPosition + 1] = data.toString();
	}

	public void close() {
		if (!Scheduler.isLoggingEnabled()) {
			return;
		}
		try {
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createHeadings() {
		if (!Scheduler.isLoggingEnabled()) {
			return;
		}
		try {
			fileWriter.write(now + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.dataHeadings[0] = "Elapsed Time";
		System.arraycopy(dataHeadings, 0, dataLine, 0, dataHeadings.length);
		updateLoop(false);
	}
}
