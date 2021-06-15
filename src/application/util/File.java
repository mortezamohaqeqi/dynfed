package application.util;

import java.io.IOException;
import java.io.PrintWriter;

public class File 
{
	public static void save(String path, String txt) 
	{
		try {
			PrintWriter writer = new PrintWriter(path, "UTF-8");
			writer.println(txt);
			writer.close();
		} catch (IOException e) {
			System.out.println("Error in saving file " + path);
			e.printStackTrace();
		}
	}

	public static void mkdir(String path) 
	{
		java.io.File file = new java.io.File(path);
        if (!file.exists()) {
            if (file.mkdir()) {
                // System.out.println("Directory is created!");
            } else {
                System.out.println("Failed to create directory!");
            }
        }		
	}
	
	public static boolean exists(String path) 
	{
		return new java.io.File(path).exists();
	}
}
