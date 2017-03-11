package net.johnhany.wpcrawler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
 * @main
 * start from here
 */
public class crawler {
	
	public static void main(String args[]) throws Exception {
		String frontpage = "http://weather.unisys.com/hurricane/atlantic/index.php";
		Connection conn = null;
		
		//connect the MySQL database
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String dburl = "jdbc:mysql://localhost:3306?useUnicode=true&characterEncoding=UTF-8";
			conn = DriverManager.getConnection(dburl, "root", "123456");
			System.out.println("connection built");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		String sql = null;
		String url = frontpage;
		Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int count = 0;
		
		if(conn != null) {
			//create database and tables that will be needed
			try {
				sql = "CREATE DATABASE IF NOT EXISTS Storm";
				stmt = conn.createStatement();
				stmt.executeUpdate(sql);
				
				
				sql = "USE Storm";
				stmt = conn.createStatement();
				stmt.executeUpdate(sql);
				
				sql = "create table if not exists record (recordID int(5) not null auto_increment, URL varchar(255) not null, crawled tinyint(1) not null, primary key (recordID)) engine=InnoDB DEFAULT CHARSET=utf8";
				stmt = conn.createStatement();
				stmt.executeUpdate(sql);
				stmt.executeUpdate("truncate record");
				
				sql = "create table if not exists data_atlantic (stormID int(5) not null auto_increment,year int(4) not null, storm_name varchar(255) not null,primary key (stormID)) engine=InnoDB DEFAULT CHARSET=utf8";
				stmt = conn.createStatement();
				stmt.executeUpdate(sql);
				
				sql = "INSERT INTO record (URL, crawled) VALUES ('" + frontpage + "',0)";
            			pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            			pstmt.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			//crawl every link in the database
			while(true) {
				//get page content of link "url"
				httpGet.getByString(url,conn); //将数据存入到数据库中
				count++;
				
				//set boolean value "crawled" to true after crawling this page
				sql = "UPDATE record SET crawled = 1 WHERE URL = '" + url + "'";
				stmt = conn.createStatement();
				
				if(stmt.executeUpdate(sql) > 0) {
					//get the next page that has not been crawled yet
					sql = "SELECT * FROM record WHERE crawled = 0";
					stmt = conn.createStatement();
					rs = stmt.executeQuery(sql);
					if(rs.next()) {
						url = rs.getString(2);
					}else {
						//stop crawling if reach the bottom of the list
						break;
					}

					//set a limit of crawling count
					if(count > 1000 || url == null) {
						break;
					}
				}
				rs.close();
				stmt.close();		
			}
			conn.close();
			conn = null;
			
			System.out.println("Done.");
			System.out.println(count);
		}
	}
}
