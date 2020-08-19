package database;

public class Query {

	public static final String CHECK_USER = "select * from uzytkownik where login=? and haslo=?;";
	public static final String INSERT_DEVICE = "insert into urzadzenie values(null,?,?,1,?,?,?,false,?,false,false,false,null);";
	public static final String GET_TYPES = "select id, nazwa from typ;";
	public static final String DEVICE_IN_SERVICE = "select id from urzadzenie where (sn like ? or sn2 like ?) and stan='N' and serwis=1 and utyl=0;";
	public static final String GET_DEVICE_FOR_UPDATE = "select * from urzadzenie where id=? for update;";
	public static final String RETURN_FROM_SERVICE = "update urzadzenie set lok_id=1, stan='S', serwis=0, opis='' where id = ?";
	public static final String DEVICE_IN_OTHER_WAR = "select id from urzadzenie where lok_id != 1 and (sn like ? or sn2 like ?);";
	public static final String RETURN_FROM_OTHER_WAR = "update urzadzenie set lok_id=1, stan=?, opis=? where id=?";
	public static final String GET_MODELS = "select id, nazwa from model;";
	public static final String DEVICE_EXIST = "select id from urzadzenie where (sn like ? or sn2 like ?) and lok_id=1 and serwis=0 and utyl=0;";
	public static final String PRINTER_FOR_UPDATE = "select id from urzadzenie where (sn like ? or sn2 like ?) and lok_id=1 and serwis=0 and utyl=0 and stan='S' and rez=0 and typ_id=3 for update;";
	public static final String GET_LOK_ID = "select id from lokalizacja where skrot=? and widoczna=1;";
	public static final String SEND_PRINTER = "update urzadzenie set lok_id=? where id=?;";
	public static final String GET_DEVICE_FOR_UPDATE_BY_SN = "select * from urzadzenie where (sn like ? or sn2 like ?) for update;";
	public static final String SEND_PRINTER_TO_SERVICE = "update urzadzenie set serwis=1 where (sn like ? or sn2 like ?) and utyl=0 and rez=0 and stan='N' and typ_id=3 and lok_id=1;";
}
