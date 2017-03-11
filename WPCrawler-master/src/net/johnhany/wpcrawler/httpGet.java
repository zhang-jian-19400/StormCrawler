package net.johnhany.wpcrawler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/*
 * WPsiteCrawler
 * a web crawler for single WordPress site
 * Author:	John Hany
 * Site:	http://johnhany.net/
 * Source code updates:	https://github.com/johnhany/WPCrawler
 * 
 * Using:	Apache HttpComponents 4.3 -- http://hc.apache.org/
 * 			HTML Parser 2.0 -- http://htmlparser.sourceforge.net/
 * 			MySQL Connector/J 5.1.27 -- http://dev.mysql.com/downloads/connector/j/
 * Thanks for their work!
 */

/*
 * @getByString
 * get page content from an url link
 */
public class httpGet {
	private static int deepth=0;
	public final static void getByString(String url,Connection conn) throws Exception{
		CloseableHttpClient httpclient = HttpClients.createDefault();
        
        try {
            HttpGet httpget = new HttpGet(url);
            System.out.println("executing request " + httpget.getURI());
            //对拿到的response对象进行修改
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }
            };
            String responseBody = httpclient.execute(httpget, responseHandler);
            //如果URL是以.dat结尾的，则把文件存起来，更新data_atlantic表，并且不用往下去分析文本内容
            if(url.endsWith(".dat")){
            	//去除后面三个字段，存入数据库，存入文件中
            	String attributes[] = url.split("/");
            	String year = attributes[attributes.length-3];
            	String name = attributes[attributes.length-2];  
            	//更新data_Atlantic表
            	String sql = null;
				ResultSet rs = null;
				PreparedStatement pstmt = null;
				Statement stmt = null;
				try {
					sql = "SELECT * FROM data_atlantic WHERE year='"+year+"' and storm_name='"+name+"'";
					stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
					rs = stmt.executeQuery(sql);
					if(rs.next()) {
						//如果有相应的地址就不再插入，否则插入
		            }else {
		            	//if the link does not exist in the database, insert it
		            	sql = "INSERT INTO data_atlantic (year, storm_name) VALUES ('"+year+"','"+name+"')";
		            	pstmt = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
		            	pstmt.execute();  
		            	FileProcess.CreateFile("D://java_project//stormdata//hurricane//"+year, name+".dat", responseBody);
		            }
				} catch (SQLException e) {
					//handle the exceptions
					System.out.println("SQLException: " + e.getMessage());
				    System.out.println("SQLState: " + e.getSQLState());
				    System.out.println("VendorError: " + e.getErrorCode());
				} finally {
					//close and release the resources of PreparedStatement, ResultSet and Statement
					if(pstmt != null) {
						try {
							pstmt.close();
						} catch (SQLException e2) {}
					}
					pstmt = null;
					
					if(rs != null) {
						try {
							rs.close();
						} catch (SQLException e1) {}
					}
					rs = null;
					
					if(stmt != null) {
						try {
							stmt.close();
						} catch (SQLException e3) {}
					}
					stmt = null;
				}
			}
            else
            parsePage.parseFromString(url,responseBody,conn);
            
        } finally {
            httpclient.close();
        } 
	}
	//参数中加了deep说明有深度限制。
    public final static void getByString(String url, Connection conn,int deep) throws Exception {
    	if(deep<++deepth) return ;
    	getByString(url,conn);
    }    
}