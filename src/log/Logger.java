package log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

public class Logger {

	private final String fileName;
	
	public Logger() {
		this.fileName = "log.txt";
	}
	
	public void log(String error) {
		BufferedWriter writer=null;
		try {
			writer = new BufferedWriter(new FileWriter(this.fileName));
			writer.append(DateFormat.getInstance().format(new Date()));
			writer.append("\n");
			writer.append(error);
			writer.append("\n");
			writer.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		finally {
			if(writer!=null)
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}	
		}
	}
}
