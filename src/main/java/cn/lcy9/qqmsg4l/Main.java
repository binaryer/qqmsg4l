package cn.lcy9.qqmsg4l;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

import jodd.http.HttpRequest;

public class Main {

	static interface User32 extends StdCallLibrary {
		User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class);

		interface WNDENUMPROC extends StdCallCallback {
			boolean callback(Pointer hWnd, Pointer arg);
		}

		boolean EnumWindows(WNDENUMPROC lpEnumFunc, Pointer userData);

		int GetWindowTextA(Pointer hWnd, byte[] lpString, int nMaxCount);
		
		int GetClassNameA(Pointer hWnd, byte[] lpString, int nMaxCount);

		//Pointer GetWindow(Pointer hWnd, int uCmd);
	}
 
	public static List<String> getAllWindowNames() {
		
		System.setProperty("jna.encoding", "GBK");
		
		final List<String> windowNames = new ArrayList<>();
		final User32 user32 = User32.INSTANCE;
		user32.EnumWindows(new User32.WNDENUMPROC() {
  
			@Override
			public boolean callback(Pointer hWnd, Pointer arg) {
				final byte[] windowText1 = new byte[512];
				final byte[] windowText2 = new byte[512];
				
				user32.GetWindowTextA(hWnd, windowText1, 512);
				final String wText1 = Native.toString(windowText1).trim();
				
				user32.GetClassNameA(hWnd, windowText2, 512);
				final String wText2 = Native.toString(windowText2).trim();
				
				if (!wText1.isEmpty()) {
					try {
						windowNames.add(new String(wText1.getBytes(), "gbk")+ "\t" + wText2);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//System.out.println(wText1 + "\t" + wText2);
				}
				
				
				return true;
			}
		}, null);

		return windowNames;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		
		//final List<String> winNameList = getAllWindowNames();
		
		//final List<String> windownames = FileUtils.readLines(new File(args[0]), "utf-8");
		
		if(args.length != 1){
			System.out.println("Usage:\n\tjava -jar qq_notice.jar <server url>\n\nhttps://github.com/binaryer/qqmsg4l");
			return;
		}
		
		while(true){
			Thread.sleep(3000L);
			
			final StringBuffer sbtitles = new StringBuffer();
			for (final String winName : getAllWindowNames()) {
				
				final String[] winNames = winName.trim().split("\t");
				
				final String title = winNames[0].trim();
				final String classname = winNames[1].trim();
				if(title.equals(""))	continue;
				if(!classname.equals("TXGuiFoundation"))	continue;
				if(title.equals("QQ"))	continue;
				if(title.equals("TXMenuWindow"))	continue;
				sbtitles.append(title).append('\n');
				//System.out.println(title);
				
			}
			
			try{
				HttpRequest.post(args[0])
				.charset("gbk")
				.form("action", "notice", "titles", sbtitles.toString())
				.send();
			}catch(Throwable t){
				t.printStackTrace();
			}

		}
		
		
	}

}
