import java.sql.*;

import org.json.*;
public class CollectionHelper
{
	private static CollectionHelper CH;
	private Connection conn = null;
	private PreparedStatement pres = null;
	
	private CollectionHelper()
	{
	}
	
	//Instantiate an EventHelper if there's none.
	public static CollectionHelper getHelper()
	{
		if( CH == null )
			CH = new CollectionHelper();
			
		return CH;
	}
	
	public JSONObject getAllDataByMemberID( String member_id )
	{
		Event e = null;
        JSONArray jsa = new JSONArray();
        
        /** 紀錄程式開始執行時間 */
        long start_time = System.nanoTime();
        
        /** 記錄實際執行之SQL指令 */
        String exexcute_sql = "";
        
        /** 紀錄SQL總行數 */
        int row = 0;
        
        /** 儲存JDBC檢索資料庫後回傳之結果，以 pointer 方式移動到下一筆資料 */
        ResultSet rs = null;
        
        try
		{
            /** 取得資料庫之連線 */
            conn = DBMgr.getConnection();
            /** SQL指令 */
            String sql = "SELECT * FROM `group5_final`.`event` INNER JOIN `group5_final`.`collection` USING (event_id) WHERE member_id = ?";
            
            /** 將參數回填至SQL指令當中，若無則不用只需要執行 prepareStatement */
            pres = conn.prepareStatement(sql);
            pres.setString( 1, member_id );
            /** 執行查詢之SQL指令並記錄其回傳之資料 */
            rs = pres.executeQuery();

            /** 紀錄真實執行的SQL指令，並印出 **/
            exexcute_sql = pres.toString();
            System.out.println(exexcute_sql);
            
            /** 透過 while 迴圈移動pointer，取得每一筆回傳資料 */
            while(rs.next())
            {
                /** 每執行一次迴圈表示有一筆資料 */
                row += 1;
                
                /** 將 ResultSet 之資料取出 */
                int collection_member_id = rs.getInt("member_id");
                int collection_id = rs.getInt("collection_id");
            	int collection_event_id = rs.getInt("event_id");
            	String title = rs.getString("event_title");
            	int eventtype_id = rs.getInt("eventtype_id");
            	String introduction = rs.getString("event_introduction");
            	String place = rs.getString("event_place");
            	Timestamp start_date = rs.getTimestamp("event_start_date");
            	Timestamp end_date = rs.getTimestamp("event_end_date");
            	String notification = rs.getString("event_notification");
            	String imagePath = rs.getString("event_image");
            	byte isOutDated = rs.getByte("event_isOutDated");
            	byte isCanceled = rs.getByte("event_isCanceled");
            	Timestamp modified = rs.getTimestamp("event_modified");
            	Timestamp created = rs.getTimestamp("event_created");
            	
            	e = new Event( collection_event_id , title , eventtype_id , introduction , place , start_date , end_date , notification , imagePath , isOutDated , isCanceled , modified , created);
            	
            	JSONObject result = e.getAllData();
            	result.put("collection_id", collection_id);
            	result.put("member_id", collection_member_id);
            	
                jsa.put( result );
            }
		}
		catch( SQLException sqlex )
		{
            /** 印出JDBC SQL指令錯誤 **/
            System.err.format("SQL State: %s\n%s\n%s", sqlex.getErrorCode(), sqlex.getSQLState(), sqlex.getMessage());
		}
		catch( Exception ex )
		{
            /** 若錯誤則印出錯誤訊息 */
            ex.printStackTrace();
		}
		finally
		{
            /** 關閉連線並釋放所有資料庫相關之資源 **/
            DBMgr.close(rs, pres, conn);
		}
		
        long end_time = System.nanoTime();
        /** 紀錄程式執行時間 */
        long duration = (end_time - start_time);
        
        /** 將SQL指令、花費時間、影響行數與所有會員資料之JSONArray，封裝成JSONObject回傳 */
        JSONObject response = new JSONObject();
        response.put("sql", exexcute_sql);
        response.put("row", row);
        response.put("time", duration);
        response.put("data", jsa);

        return response;
	}
	
	public JSONObject createCollection( Collection c )
	{   
		int collection_id = -1;
		
        /** 紀錄程式開始執行時間 */
        long start_time = System.nanoTime();
        
        /** 記錄實際執行之SQL指令 */
        String exexcute_sql = "";
        
        /** 紀錄SQL總行數 */
        int row = 0;
		
		try
		{
			conn = DBMgr.getConnection();
			
			String sql = "INSERT INTO group5_final.collection( `event_id` , `member_id` )"
					+ " VALUES( ? , ? )";
			
			int event_id = c.getEventID();
			int member_id = c.getMemberID();
			
			pres = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			pres.setInt( 1 , event_id );
			pres.setInt( 2 , member_id );
			
            /** 執行新增之SQL指令並記錄影響之行數 */
            row = pres.executeUpdate();
            
            /** 紀錄真實執行的SQL指令，並印出 **/
            exexcute_sql = pres.toString();
            System.out.println(exexcute_sql);
            
            //取得插入數值所產生的主鍵
            ResultSet rs = pres.getGeneratedKeys();
            
            if (rs.next())
            	collection_id = rs.getInt(1);
		}
		catch( SQLException e )
		{
			System.err.format("SQL State: %s\n%s\n%s", e.getErrorCode(), e.getSQLState(), e.getMessage());
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
            DBMgr.close(pres, conn);
		}
		
        /** 紀錄程式結束執行時間 */
        long end_time = System.nanoTime();
        /** 紀錄程式執行時間 */
        long duration = (end_time - start_time);
		
		JSONObject response = new JSONObject();
        response.put("sql", exexcute_sql);
        response.put("row", row);
        response.put("time", duration);
        response.put("collection_id", collection_id);
		
		return response;
	}

	public JSONObject deleteByCollectionID( String collection_id )
	{
        /** 記錄實際執行之SQL指令 */
        String exexcute_sql = "";
        System.out.printf("CollectionID:%s",collection_id);
		
        /** 紀錄程式開始執行時間 */
        long start_time = System.nanoTime();
        
        /** 紀錄SQL總行數 */
        int row = 0;
		
		try
		{
			conn = DBMgr.getConnection();
			
			String sql = "DELETE FROM group5_final.collection WHERE collection_id = ?";
			
			pres = conn.prepareStatement(sql);
			pres.setString( 1 , collection_id );
			
            /** 執行新增之SQL指令並記錄影響之行數 */
            row = pres.executeUpdate();
            
            /** 紀錄真實執行的SQL指令，並印出 **/
            exexcute_sql = pres.toString();
            System.out.println(exexcute_sql);
		}
		catch( SQLException e )
		{
			System.err.format("SQL State: %s\n%s\n%s", e.getErrorCode(), e.getSQLState(), e.getMessage());
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
            DBMgr.close(pres, conn);
		}
		
        /** 紀錄程式結束執行時間 */
        long end_time = System.nanoTime();
        /** 紀錄程式執行時間 */
        long duration = (end_time - start_time);
		
        JSONObject response = new JSONObject();
        response.put("sql", exexcute_sql);
        response.put("row", row);
        response.put("time", duration);

        return response;
	}
}
