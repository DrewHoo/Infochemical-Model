package repast.model.heatbugs;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Logger {
	private String info;
	private String batchID;
	private String directory;
	private OutputStream out;
	
	public Logger(String batchID, String directory) {
		this.batchID = batchID;
		this.directory = directory;
		Path p = Paths.get(directory + batchID + ".txt");
		try {
			out = new BufferedOutputStream(
				Files.newOutputStream(p, CREATE, APPEND));
		} catch (IOException e) {
			System.err.println(e);
			System.out.println("Error in logger constructor");
		}
		info = "";
	}
	public void eat(String s) {
		info += s;
	}
	public void dumpInfo() {
		try {
			out.write(info.getBytes(), 0, info.getBytes().length);
		} catch (IOException e) {
			System.err.println(e);
		}
	}
	public String getDirectory() {
		return directory;
	}
	public void setDirectory(String s) {
		directory = s;
	}
}
