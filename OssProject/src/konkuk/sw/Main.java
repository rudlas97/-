package konkuk.sw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Main {

	public static void main(String[] args) throws IOException, ParseException {
		// TODO Auto-generated method stub
		Scanner scan = new Scanner(System.in);
		String result;
		System.out.print("주소입력:");
		String input = scan.nextLine(); //주소입력
		String[] str = input.split(" "); //공백을 기준으로 분리
		String areaTop=str[0]; //
		String areaMid=str[1]; //
		String areaLeaf=str[2]; //
		String code="";
		String x = "";
		String y = "";
		SimpleDateFormat format1 = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat format2 = new SimpleDateFormat("HH");
		Calendar cal = Calendar.getInstance();
		String date = format1.format(cal.getTime()); //발표날짜
		String time = format2.format(cal.getTime()); //발표시간
		String ftime = time + "00"; //관측시간
		//System.out.println(ftime);
		
		if(Integer.parseInt(time) == 0) {
			time = "2300";
		}else {
			time = Integer.parseInt(time)-1 + "00";
		}
		
		if(Integer.parseInt(time) < 1000) time="0"+time;
		System.out.println("<" + areaMid + " " + areaLeaf + ">");
		
		URL url;
		BufferedReader br;
		URLConnection conn;
		
		JSONParser parser;
		JSONArray jArr;
		JSONObject jobj;
		
		url = new URL("http://www.kma.go.kr/DFSROOT/POINT/DATA/top.json.txt");
		conn = url.openConnection();
		br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		result = br.readLine().toString();
		br.close();
		//System.out.println(result);
		
		parser = new JSONParser();
		jArr = (JSONArray)parser.parse(result);
		for(int i=0; i < jArr.size(); i++) {
			jobj = (JSONObject)jArr.get(i);
			if(jobj.get("value").equals(areaTop)) {
				code=(String)jobj.get("code");
				//System.out.println(code);
				break;
			}
		}
		
		url = new URL("http://www.kma.go.kr/DFSROOT/POINT/DATA/mdl." + code + ".json.txt");
		conn = url.openConnection();
		br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		result = br.readLine().toString();
		br.close();
		//System.out.println(result);
		
		parser = new JSONParser();
		jArr = (JSONArray)parser.parse(result);
		
		for(int i=0; i < jArr.size(); i++) {
			jobj = (JSONObject)jArr.get(i);
			if(jobj.get("value").equals(areaMid)) {
				code = (String)jobj.get("code");
				//System.out.println(code);
				break;
			}
		}
		
		url = new URL("http://www.kma.go.kr/DFSROOT/POINT/DATA/leaf." + code + ".json.txt");
		conn = url.openConnection();
		br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		result = br.readLine().toString();
		br.close();
		//System.out.println(result);
		
		parser = new JSONParser();
		jArr = (JSONArray)parser.parse(result);
		
		if(areaMid.equals("종로구")) {
			for(int i=0; i < jArr.size(); i++) {
				jobj = (JSONObject)jArr.get(i);
				
				String leaf1 = areaLeaf.substring(0,areaLeaf.length()-3);
				String leaf2 = areaLeaf.substring(areaLeaf.length()-3, areaLeaf.length()-2);
				String leaf3 = areaLeaf.substring(areaLeaf.length()-2,areaLeaf.length());
				
				Pattern pattern = Pattern.compile(leaf1+"[1-9.]{0,8}"+leaf2+"[1-9.]{0,8}"+leaf3);
        		Matcher matcher = pattern.matcher((String) jobj.get("value"));
        		if(matcher.find()) {
            		x=(String)jobj.get("x");
            		y=(String)jobj.get("y");
            		//System.out.println(areaLeaf+"의 x값 : "+x+", y값 :"+y);
            		break;
            	}
			}
		}else {
			for(int i=0; i<jArr.size(); i++) {
				jobj = (JSONObject)jArr.get(i);
				if(jobj.get("value").equals(areaLeaf)) {
					x=(String)jobj.get("x");
					y=(String)jobj.get("y");
					//System.out.println(x + " " + y);
					break;
				}
			}
		}
		
		String apiUrl ="http://apis.data.go.kr/1360000/VilageFcstInfoService/getUltraSrtFcst";
		String apiKey = ""; //key 입력
		apiUrl += "?serviceKey=" + apiKey;
		apiUrl += "&pageNo=1&numOfRows=100";
		apiUrl += "&dataType=JSON";
		apiUrl += "&base_date=" + date;
		apiUrl += "&base_time=" + time;
		apiUrl += "&nx=" + x;
		apiUrl += "&ny=" + y;
		//System.out.println(apiUrl);
		
		url = new URL(apiUrl);
		conn = url.openConnection();
		br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		result = br.readLine().toString();
		br.close();
		
		parser = new JSONParser();
		JSONObject obj = (JSONObject)parser.parse(result);
		JSONObject response = (JSONObject)obj.get("response");
		JSONObject body = (JSONObject)response.get("body");
		JSONObject items = (JSONObject)body.get("items");
		JSONArray item = (JSONArray)items.get("item");
		int count = 0;
		for(int i=0; i < item.size(); i++) {
			jobj = (JSONObject)item.get(i);
			if(jobj.get("fcstTime").equals(ftime)) {
				if(jobj.get("category").equals("SKY")) {
					System.out.print("하늘상태:");
					String sky = "";
					if(jobj.get("fcstValue").equals("0")) {
						sky = "맑음";
					}else if(jobj.get("fcstValue").equals("3")) {
						sky = "구름많음";
					}else {
						sky = "흐림";
					}
					System.out.println(sky);
					count++;
				}else if(jobj.get("category").equals("T1H")) {
					System.out.print("기온:");
					System.out.println(jobj.get("fcstValue") + "℃");
					count++;
				}else if(jobj.get("category").equals("PTY")) {
					System.out.print("강수형태:");
					String pty = "";
					if(jobj.get("fcstValue").equals("0")) {
						pty = "없음";
					}else if(jobj.get("fcstValue").equals("1")){
						pty = "비";
					}else if(jobj.get("fcstValue").equals("2")) {
						pty = "비/눈";
					}else if(jobj.get("fcstValue").equals("3")) {
						pty = "눈";
					}else {
						pty = "소나기";
					}
					System.out.println(pty);
					count++;
				}else if(jobj.get("category").equals("REH")) {
					System.out.print("습도:");
					System.out.println(jobj.get("fcstValue") + "%");
					count++;
				}else if(jobj.get("category").equals("RN1")) {
					System.out.print("강수량:");
					System.out.println(jobj.get("fcstValue") + "mm");
					count++;
				}
			}
			if(count == 5)break;
		}
		
	}
	

}
