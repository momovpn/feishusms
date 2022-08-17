package com.util.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.json.JSONObject;

public class SendMessage {
	
	static String app_id = "cli_a26dc5f1af7ed00e";  //provided by the customer
	static String app_secret = "m70gBwdypcjiyOl1BEXa6fxDunkEUfgj";  //provided by the customer

	public static void main(String[] args) throws Exception {
    	
//    	SendWeChat msg = new SendWeChat("conf.properties");
        String token = fetchToken(); //generate the token
        System.out.println(token);

        sendSMS(token);
    }

	private static void sendSMS(String token) throws Exception {
		String url = "https://open.feishu.cn/open-apis/im/v1/messages?receive_id_type=user_id";
		System.out.println(url);
      
		String user = "c834eadg";     //the same with mobile - %mobNo%, because the name contains contain letters�� there should no limit for the mobile value
		String msgtype = "text";
		String content = "重置密码fdgdfgdfgfdg码: 891362";   //sent message  - %message%
		
		String message = "{\"text\":\"" + content + "\"}";
//      {\"text\":\"重置密码验证码: sasa \"}
		JSONObject postdata = new JSONObject();
		postdata.put("receive_id", user);
		postdata.put("msg_type", msgtype);
		postdata.put("content", message);
      
      	System.out.println(postdata);
      
      
      	String str = sendFSMessage(url, postdata, token);
      	System.out.println(str);
	}

	private static String sendFSMessage(String url, JSONObject postdata, String token) throws Exception {
		PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("Authorization", "Bearer " + token); //HTTP request header
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());
            out.print(postdata);
            out.flush();
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
	}

	private static String fetchToken() throws Exception {
	
		Properties prop = new Properties();
		String token;
		int expires;
		Long time;
		String url = null;
		try{
			prop.load(new FileInputStream("st.properties"));
			expires = Integer.valueOf(prop.getProperty("expire"));
			time = Long.valueOf(prop.getProperty("time"));
			token = prop.getProperty("access_token");
			if ((System.currentTimeMillis()/1000 < (time/1000+expires))){
				return token;
			}
		}catch (IOException e){
          
		}
      
		if ((app_id != null) && (app_secret != null)){
			url = "https://open.feishu.cn/open-apis/auth/v3/app_access_token/internal";
		}else{
			System.out.println("[!] Your account type is not correct.");
			return null;
		}
      
		String getTokenRes;
      
		try {
			getTokenRes = getRequest(url); //post method to generate the token
		} catch (Exception e1) {
			return null;
		}
      
		System.out.println("[-] get token API response:\n"+getTokenRes);
		JSONObject jobj = new JSONObject(getTokenRes);
		token =jobj.getString("app_access_token");
		expires =jobj.getInt("expire");
		time = System.currentTimeMillis();
		prop.setProperty("access_token", token);
		prop.setProperty("expire", String.valueOf(expires));
		prop.setProperty("time", String.valueOf(time));
		prop.store(new FileOutputStream("st.properties"), "store the new access token");
		return token;
	}

	private static String getRequest(String url) throws IOException {
		StringBuffer bs = new StringBuffer();
		URL sendUrl = new URL(url.trim());
		JSONObject data = new JSONObject();
		data.put("app_id", app_id);
		data.put("app_secret", app_secret);
		URLConnection connection = sendUrl.openConnection();
		connection.setConnectTimeout(30000);
		connection.setReadTimeout(30000);
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type","application/json; charset=UTF-8");
		OutputStreamWriter out = new OutputStreamWriter(
				connection.getOutputStream(), "UTF-8");
		out.write(data.toString());
		out.flush();
		out.close();
		connection.connect();
		InputStream is = connection.getInputStream();
		BufferedReader buffer = new BufferedReader(new InputStreamReader(is,"UTF-8"));
 
		String l = null;
		while ((l = buffer.readLine()) != null) {
			bs.append(l);
		}
		return bs.toString();
	}
}
