package telnetserver;

import java.awt.EventQueue;
import java.io.IOException;
import java.net.ServerSocket;

public class TelnetServer {

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				ServerSocket ss = null;
				try{
					ss = new ServerSocket(23);
					while(true){
						new ServerThread(ss.accept()).start();
					}
				}
				catch(IOException e){
					e.printStackTrace();
				}
				finally {
					try {
						if(ss!=null) ss.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

}
