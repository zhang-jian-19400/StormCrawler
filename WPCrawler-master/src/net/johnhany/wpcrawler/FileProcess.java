package net.johnhany.wpcrawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileProcess {
	public static void CreateFile(String filePath,String fileName,String Content) throws IOException{
		File filedir = new File(filePath);
		File targetfile = new File(filePath+"//"+fileName);
		if(filedir.exists())
			if(targetfile.exists()) return ;		
			else{
				File newfile = new File(filePath+"//"+fileName);
				FileWriter writer = new FileWriter(newfile,true);
				writer.write(Content);
				writer.close();
			}
		else{
			filedir.mkdir();
			File newfile = new File(filePath+"//"+fileName);
			FileWriter writer = new FileWriter(newfile,true);
			writer.write(Content);
			writer.close();
		}
		
	}
}
