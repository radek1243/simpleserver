package telnetserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import database.DBManager;
import database.Device;
import database.Query;
import log.Logger;

public class ServerThread extends Thread {

	private Socket socket;
	private BufferedReader reader;
	private BufferedWriter writer;
	private String line;
	private boolean isConnected;
	private boolean isLogged;
	private String user;
	private String pass;
	private DBManager man;
	private boolean firstLogIn;
	private Device device;
	private int id;
	private int result;
	private Connection con;
	private HashMap<String, Integer> typesMap;
	private HashMap<String, Integer> modelsMap;
	private boolean typeCorrect;
	private boolean modelCorrect;
	private Integer data;
	private boolean lokIdCorrect;
	
	public ServerThread(Socket socket) {
		this.socket = socket;
		try{
			this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
			this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream(), "UTF-8"));
			this.isConnected=true;
			this.isLogged = false;
			this.man = new DBManager();
			this.firstLogIn=true;
			this.device = new Device();
			this.id = -1;
			this.socket.setSoTimeout(1800000);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try{
			System.out.println("Po³¹czono.");
			while(this.isConnected){
				if(this.isLogged){
					this.con.commit();
					this.typesMap = this.man.getTypes(this.con);
					this.modelsMap = this.man.getModels(this.con);
					this.writer.write("\n\rCo chcesz zrobic?\n\r");
					this.writer.write("1 - wyslanie drukarki\n\r");
					this.writer.write("2 - dodanie urzadzenia\n\r");
					this.writer.write("3 - wyslij drukarke na serwis\n\r");
					this.writer.flush();
					if((this.line=this.reader.readLine())!=null){
						if(this.line.equals("1")){
							this.lokIdCorrect = false;
							this.writer.write("\n\rZeskanuj numer seryjny:\n\r");
							this.writer.flush();
							if((this.line=this.reader.readLine())!=null){
								this.device.setSN(this.line);
								while(!this.lokIdCorrect){
									this.writer.write("\n\rPodaj skrot placowki\n\r");
									this.writer.flush();
									if((this.line=this.reader.readLine())!=null){
										this.id = this.man.getLocId(this.line, this.con);
										if(this.id>-1) this.lokIdCorrect = true;
									}
								}		
								this.result = this.man.sendPrinter(this.device.getSN(), this.id, this.con);
								if(this.result == -1){
									this.writer.write("\n\rNie wys³ano drukarki\n\r");
									this.writer.flush();
								}
								else{
									this.writer.write("\n\rWys³ano drukarkê\n\r");
									this.writer.flush();
								}
							}
						}
						else if(this.line.equals("2")){							
							//tutaj algorytm
							this.writer.write("Zeskanuj numer seryjny:\n\r");
							this.writer.flush();
							if((this.line=this.reader.readLine())!=null){								
								this.device.setSN(this.line);
								this.result = this.man.deviceExist(this.device.getSN(), this.con);	//czy urzadzenie istnieje na kanciapie
								//System.out.println(this.result);
								if(this.result==-1){	//jesli nie znaleziono urzadzenia
									this.id = this.man.getDeviceId(this.line, Query.DEVICE_IN_SERVICE, this.con);	//sprawdzamy czy urz¹dzenie wróci³o z serwisu
									//System.out.println(this.line);
									if(this.id==-1){	//jeœli nie wrocila z serwsiu
										//trzeba sprawdziæ czy wróci³a z innej placówki
										this.id = this.man.getDeviceId(this.line, Query.DEVICE_IN_OTHER_WAR, this.con);
										if(this.id==-1){	//jeœli nie wróci³a z innej placówki
											this.writer.write("\n\rTrzeba dodac urzadzenie");
											this.writer.flush();
											this.typeCorrect = false; this.modelCorrect = false; 
											//this.device.reset();
											if(this.device.getType()!=-1 && this.device.getModel()!=-1){
												this.writer.write("\n\rCzy chcesz dodac\n\r");
												this.writer.write("ten sam typ i model urz.\n\r");
												this.writer.write("1 - tak\n\r");
												this.writer.write("2 - nie\n\r");
												this.writer.flush();
												this.line=this.reader.readLine();
											}
											if(!this.line.equals("1")){
												while(!this.typeCorrect){
													this.writer.write("\n\rPodaj typ:\n\r");
													this.writer.flush();
													if((this.line=this.reader.readLine())!=null){
														this.data = this.typesMap.get(this.line);
														if(this.data!=null){
															this.device.setType(this.data.intValue());
															this.typeCorrect = true;
														}
													}
												}
												while(!this.modelCorrect){
													this.writer.write("\n\rPodaj model:\n\r");
													this.writer.flush();
													if((this.line=this.reader.readLine())!=null){
														this.data = this.modelsMap.get(this.line);
														if(this.data!=null){
															this.device.setModel(this.data.intValue());
															this.modelCorrect = true;
														}
													}
												}
											}
											this.writer.write("\n\rPodaj drugi numer seryjny:\n\r");
											this.writer.flush();
											if((this.line=this.reader.readLine())!=null){
												this.device.setSN2(this.line);
												this.writer.write("\n\rPodaj stan (S lub N):\n\r");
												this.writer.flush();
												if((this.line=this.reader.readLine())!=null){
													this.device.setStan(this.line);
													this.writer.write("\n\rPodaj opis:\n\r");
													this.writer.flush();
													if((this.line=this.reader.readLine())!=null){
														this.device.setDesc(this.line);
														this.result = this.man.insertDevice(this.con, this.device.getType(), this.device.getModel(), 
																this.device.getSN(), this.device.getSN2(), this.device.getStan(), this.device.getDesc());
														if(this.result==1){
															this.writer.write("\n\rDodano urzadzenie");
															this.writer.flush();
														}
														else{
															this.writer.write("\n\rNie dodano urzadzenia");
															this.writer.flush();
														}
													}
												}
											}
											//metoda dodawania urz¹dzenia
										}
										else{
											this.writer.write("\n\rUrzadzenie wraca z innej placowki");
											this.writer.write("\n\rPodaj stan (S lub N):\n\r");
											this.writer.flush();
											if((this.line=this.reader.readLine())!=null){
												this.device.setStan(this.line);
												this.writer.write("\n\rPodaj opis:\n\r");
												this.writer.flush();
												if((this.line=this.reader.readLine())!=null){
													this.device.setDesc(this.line);
													this.result = this.man.returnFromOtherWar(this.id, this.device.getStan(), this.device.getDesc(), this.con);
													if(this.result==-1){
														this.writer.write("\n\rBlad przywracania urzadzenia");
														this.writer.flush();
													}
													else{
														this.writer.write("\n\rPrzywrocono urzadzenie");
														this.writer.flush();
													}
												}
											}
										}
									}
									else{	//jeœli wrocila z serwsiu
										//przywracamy urz¹dzenie lok_id=1, stan='S', serwis=0, opis='';
										this.result = this.man.returnFromService(this.id, this.con);
										if(this.result!=-1){
											this.writer.write("\n\rPrzywrocono urzadzenie");
											this.writer.flush();
										}
										else{
											this.writer.write("\n\rBlad przywracania urzadzenia");
											this.writer.flush();
										}
									}
								}
								else{	//jesli znaleziono urzadzenie
									this.writer.write("\n\rTaki SN jest juz dodany");
									this.writer.flush();
								}
							}
						}
						else if(this.line.equals("3")) {
							this.writer.write("\n\rZeskanuj numer seryjny:\n\r");
							this.writer.flush();
							if((this.line=this.reader.readLine())!=null){
								this.device.setSN(this.line);								
								this.result = this.man.sendPrinterToService(this.device.getSN(), this.con);
								if(this.result == 1){
									this.writer.write("\n\rWys³ano drukarkê na serwis\n\r");
									this.writer.flush();
								}
								else {
									this.writer.write("\n\rDrukarka nie spe³nia warunków serwisu\n\r");
									this.writer.flush();
								}
							} 
						}
					}
				}
				else{
					this.logIn();
				}
			}
			this.socket.close();
			this.con.close();
		}
		catch(IOException e){
			new Logger().log(e.getLocalizedMessage());
			e.printStackTrace();
			try {
				this.socket.close();
				this.con.close();
			} 
			catch (IOException e1) {
				new Logger().log(e.getLocalizedMessage());
				e1.printStackTrace();
			}
			catch(SQLException e2){
				new Logger().log(e.getLocalizedMessage());
				e2.printStackTrace();
			}
		}
		catch(SQLException e){
			new Logger().log(e.getLocalizedMessage());
			e.printStackTrace();
			try {
				this.socket.close();
				this.con.close();
			} 
			catch (IOException e1) {
				new Logger().log(e.getLocalizedMessage());
				e1.printStackTrace();
			}
			catch(SQLException e2){
				new Logger().log(e.getLocalizedMessage());
				e2.printStackTrace();
			}
		}
	}
	
	private void logIn() throws IOException{
		try{
			this.writer.write("\n\rPodaj login: ");		
			this.writer.flush();
			if((this.user=this.reader.readLine())!=null){
				if(this.firstLogIn){
					//this.user = this.user.substring(21, this.user.length());	//usuniêcie dziwnych znaczkow
					this.firstLogIn = false;
				}
				this.writer.write("\n\rPodaj has³o: ");
				this.writer.flush();
				if((this.pass=this.reader.readLine())!=null){
					this.isLogged = this.man.userExist(this.user, this.pass);
					if(this.isLogged){
						this.writer.write("\n\rZalogowano");
						this.writer.flush();
						this.con = this.man.connectToDB();
						this.con.setAutoCommit(false);
					}
					else{
						this.writer.write("\n\rB³êdny login lub has³o");
						this.writer.flush();
					}
				}
			}
		}
		catch(SQLException e){
			new Logger().log(e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
}
