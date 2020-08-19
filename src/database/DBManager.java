package database;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import log.Logger;

public class DBManager {
	
	private Connection con;
	private PreparedStatement prepStat;
	private ResultSet resultSet;
	private int result;
	private boolean exist;
	private StringBuilder hash;
	private MessageDigest digest;
	private byte[] buffer;

	//moï¿½e przerobiï¿½ na statica ï¿½eby byï¿½o dla wszystkich okienek jeï¿½eli to taki problem
	public DBManager() {
		this.exist = false;
		this.hash = new StringBuilder();
		try {
			this.digest = MessageDigest.getInstance("SHA-256");
		} 
		catch (NoSuchAlgorithmException e) {
			new Logger().log(e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
	
	private boolean connect(){
		try{
			this.con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/magazynit?serverTimezone=UTC&useUnicode=yes&characterEncoding=UTF-8&useSSL=false", "", "");
			return true;
		}
		catch(SQLException e){
			new Logger().log(e.getLocalizedMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	private void disconnect(){
		try{
			this.con.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	public Connection connectToDB(){
		try{
			return DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/magazynit?serverTimezone=UTC&useUnicode=yes&characterEncoding=UTF-8&useSSL=false", "", "");
		}
		catch(SQLException e){
			new Logger().log(e.getLocalizedMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	public void disconnectFromDB(Connection con){
		try{
			con.close();
		}
		catch(SQLException e){
			new Logger().log(e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
	
	public boolean userExist(String login, String pass){
		if(this.connect()){
			try{
				this.exist = false;
				this.resultSet = null;
				this.prepStat = this.con.prepareStatement(Query.CHECK_USER);
				this.prepStat.setString(1, login);
				this.prepStat.setString(2, this.hashPass(pass));
				this.resultSet = this.prepStat.executeQuery();
				this.exist = this.resultSet.next();
				this.disconnect();
				return this.exist;
			}
			catch(SQLException e){
				new Logger().log(e.getLocalizedMessage());
				e.printStackTrace();
				return false;
			}
		}
		else {
			System.out.println("B³¹d po³¹czenia z baza danych.");
			return false;
		}
	}
	
	
	public int getDeviceId(String sn, String query, Connection con){
		this.result = -1;
		try{
			this.prepStat = con.prepareStatement(query);
			this.prepStat.setString(1, sn);
			this.prepStat.setString(2, sn);
			this.resultSet = this.prepStat.executeQuery();
			if(this.resultSet.next()){
				this.result = this.resultSet.getInt(1);
			}
			this.resultSet.close();
			this.prepStat.close();
			return this.result;
		}
		catch(SQLException e){
			new Logger().log(e.getLocalizedMessage());
			e.printStackTrace();
			return -1;
		} 
	}
	
	public int returnFromService(int id, Connection con){
		this.result = -1;
		try{
			this.prepStat = con.prepareStatement(Query.GET_DEVICE_FOR_UPDATE);
			this.prepStat.setInt(1, id);
			this.resultSet = this.prepStat.executeQuery();
			if(this.resultSet.next()){
				this.prepStat.close();
				this.prepStat = con.prepareStatement(Query.RETURN_FROM_SERVICE);
				this.prepStat.setInt(1, id);
				this.result = this.prepStat.executeUpdate();
				con.commit();
				this.resultSet.close();
				this.prepStat.close();
			}
			else con.commit();
			return this.result;
		}
		catch(SQLException e){
			new Logger().log(e.getLocalizedMessage());
			e.printStackTrace();
			try {
				con.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return -1;
		} 
	}
	
	public int returnFromOtherWar(int id, String stan, String desc, Connection con){
		this.result = -1;
		try{
			this.prepStat = con.prepareStatement(Query.GET_DEVICE_FOR_UPDATE);
			this.prepStat.setInt(1, id);
			this.resultSet = this.prepStat.executeQuery();
			if(this.resultSet.next()){
				this.prepStat.close();
				this.prepStat = con.prepareStatement(Query.RETURN_FROM_OTHER_WAR);
				this.prepStat.setString(1, stan);
				this.prepStat.setString(2, desc);
				this.prepStat.setInt(3, id);
				this.result = this.prepStat.executeUpdate();
				con.commit();
				this.resultSet.close();
				this.prepStat.close();
			}
			else con.commit();
			return this.result;
		}
		catch(SQLException e){
			e.printStackTrace();
			try {
				con.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return -1;
		} 
	}
	
	public HashMap<String, Integer> getTypes(Connection con){
		try{
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			this.prepStat = con.prepareStatement(Query.GET_TYPES);
			this.resultSet = this.prepStat.executeQuery();
			while(this.resultSet.next()){
				map.put(this.resultSet.getString(2), this.resultSet.getInt(1));
			}
			this.resultSet.close();
			this.prepStat.close();
			return map;
		}
		catch(SQLException e){
			e.printStackTrace();
			return null;
		}
	}
	
	public HashMap<String, Integer> getModels(Connection con){
		try{
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			this.prepStat = con.prepareStatement(Query.GET_MODELS);
			this.resultSet = this.prepStat.executeQuery();
			while(this.resultSet.next()){
				map.put(this.resultSet.getString(2), this.resultSet.getInt(1));
			}
			this.resultSet.close();
			this.prepStat.close();
			return map;
		}
		catch(SQLException e){
			e.printStackTrace();
			return null;
		}
	}
	
	public int insertDevice(Connection con, int type_id, int model_id, String sn, String sn2, String stan, String desc){
		this.result = -1;
		try{
			this.prepStat = con.prepareStatement(Query.INSERT_DEVICE);
			this.prepStat.setInt(1, type_id);
			this.prepStat.setInt(2, model_id);
			this.prepStat.setString(3, sn);
			this.prepStat.setString(4, sn2);
			this.prepStat.setString(5, stan);
			this.prepStat.setString(6, desc);
			this.result= this.prepStat.executeUpdate();
			con.commit();
			this.prepStat.close();
			return this.result;
		}
		catch(SQLException e){
			e.printStackTrace();
			try {
				con.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return -1;
		} 
	}
	
	public int deviceExist(String sn, Connection con){
		this.result = -1;
		try{
			this.prepStat = con.prepareStatement(Query.DEVICE_EXIST);
			this.prepStat.setString(1, sn);
			this.prepStat.setString(2, sn);
			this.resultSet = this.prepStat.executeQuery();
			if(this.resultSet.next()){
				this.result = this.resultSet.getInt(1);
			}
			this.resultSet.close();
			this.prepStat.close();
			return this.result;
		}
		catch(SQLException e){
			e.printStackTrace();
			try {
				con.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return -1;
		} 
	}
	
	public int sendPrinter(String sn, int lokId, Connection con){
		this.result = -1;
		try{
			this.prepStat = con.prepareStatement(Query.PRINTER_FOR_UPDATE);
			this.prepStat.setString(1, sn);
			this.prepStat.setString(2, sn);
			this.resultSet = this.prepStat.executeQuery();
			if(this.resultSet.next()){		
				this.result = this.resultSet.getInt(1);
				this.prepStat.close();
				this.prepStat = con.prepareStatement(Query.SEND_PRINTER);
				this.prepStat.setInt(1, lokId);
				this.prepStat.setInt(2, this.result);
				this.result = -1;
				this.result = this.prepStat.executeUpdate();
			}
			this.resultSet.close();
			this.prepStat.close();
			con.commit();
			return this.result;
		}
		catch(SQLException e){
			e.printStackTrace();
			try {
				con.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return -1;
		} 
	}
	
	public int sendPrinterToService(String sn, Connection con) {
		this.result = -1;
		try{
			this.prepStat = con.prepareStatement(Query.GET_DEVICE_FOR_UPDATE_BY_SN);
			this.prepStat.setString(1, sn);
			this.prepStat.setString(2, sn);
			this.resultSet = this.prepStat.executeQuery();
			if(this.resultSet.next()){		
				this.prepStat.close();
				this.prepStat = con.prepareStatement(Query.SEND_PRINTER_TO_SERVICE);
				this.prepStat.setString(1, sn);
				this.prepStat.setString(2, sn);
				this.result = -1;
				this.result = this.prepStat.executeUpdate();
			}
			this.resultSet.close();
			this.prepStat.close();
			con.commit();
			return this.result;
		}
		catch(SQLException e){
			e.printStackTrace();
			try {
				con.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return -1;
		} 
	}
	
	public int getLocId(String sLoc, Connection con){
		this.result = -1;
		try{
			this.prepStat = con.prepareStatement(Query.GET_LOK_ID);
			this.prepStat.setString(1, sLoc);
			this.resultSet = this.prepStat.executeQuery();
			if(this.resultSet.next()){
				this.result = this.resultSet.getInt(1);
			}
			this.resultSet.close();
			this.prepStat.close();
			return this.result;
		}
		catch(SQLException e){
			e.printStackTrace();
			try {
				con.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return -1;
		} 
	}
	
	private String hashPass(String pass){
		try {
			if(this.hash.length()>0) this.hash.delete(0, this.hash.length());
			this.buffer = this.digest.digest(pass.getBytes("UTF-8"));
			for (int i = 0; i < this.buffer.length; i++) {
			    String hex = Integer.toHexString(0xff & this.buffer[i]);
			    if(hex.length() == 1) this.hash.append('0');
			        this.hash.append(hex);
		    }
			return this.hash.toString();
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
}
