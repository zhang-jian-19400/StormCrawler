package net.johnhany.wpcrawler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import java.net.URLDecoder;

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
 * @parseFromString
 * extract link from <a> tags in a web page
 * @frontPage 
 * 当前页面的地址，用于定位到相对路径使用
 */
public class parsePage {
	
	public static void parseFromString(String frontPage,String content, Connection conn) throws Exception {
		Parser parser = new Parser(content);
		//爬取地址内容
		HasAttributeFilter filter = new HasAttributeFilter("href"); 
		
		try {
			NodeList list = parser.parse(filter);
			int count = list.size();
			//获取每个地址的当前目录用以与绝对路径进行拼接
			if(frontPage.endsWith(".php")||frontPage.endsWith(".html"))
				frontPage = frontPage.substring(0,frontPage.lastIndexOf('/')+1);
			String mainurl =frontPage;
			//process every link on this page
			for(int i=0; i<count; i++) {
				Node node = list.elementAt(i);
				
				if(node instanceof LinkTag) {
					LinkTag link = (LinkTag) node;
					String nextlink = link.extractLink();					
					String wpurl = mainurl;
					if(!nextlink.startsWith("http:")) nextlink = mainurl+nextlink; //应对使用绝对路径的url
					
					//only save page from "http://weather.unisys.com/hurricane/atlantic"
					if(nextlink.startsWith(wpurl)&&(nextlink.endsWith(".dat")||nextlink.endsWith(".php"))) {
						if(nextlink.endsWith(".php")){
							String str[] = nextlink.split("/");
	//						System.out.println(str.length);
							if(Integer.parseInt(str[str.length-2])<1999) continue;
						}
						
						String sql = null;
						ResultSet rs = null;
						PreparedStatement pstmt = null;
						Statement stmt = null;
					  //先将爬取到的hurriane的超链接或者数据先存储起来
						try {
							//相当于URL管理器的作用
							//check if the link already exists in the database							
							sql = "SELECT * FROM record WHERE URL = '" +nextlink + "'";
							stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_UPDATABLE);
							rs = stmt.executeQuery(sql);
							if(rs.next()) { continue;
								//如果有相应的地址就不再插入，否则插入
				            }else {
				            	//if the link does not exist in the database, insert it
				            	sql = "INSERT INTO record (URL, crawled) VALUES ('"+nextlink+"',0)";
				            	pstmt = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
				            	pstmt.execute();
				            	System.out.println(nextlink);            	
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
	/*				
						if(nextlink.endsWith(".dat")){
			            	//use substring for better comparison performance
			            	nextlink = nextlink.substring(wpurl.length());
			            	//System.out.println(nextlink);
			            	String buff[] = nextlink.split("/");
			            	if(buff.length==3){
			            		//将文件写入数据库
			            		System.out.println(buff[0]);
			            	}
						}
						else if(nextlink.endsWith(".php")){
						//	System.out.println(nextlink);
							httpGet.getByString(nextlink, conn);
						}
	*/				}
				}
			}
		} catch (ParserException e) {
			e.printStackTrace();
		}
	}
}