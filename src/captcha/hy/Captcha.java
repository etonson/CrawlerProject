package captcha.hy;

import captcha.CaptchaImp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.Thread;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.jdom.Element;
import org.json.JSONObject;

/**
 * @author M910t
 * 火眼答題 OK
 * 
 * 原火眼答題的sample都是用JNA載入dll處理，但因為提供的是32位元的程式無法執行，參考web文件後發現與91打碼幾乎一模一樣，改用91打碼的方式執行
 */
public class Captcha extends CaptchaImp{

     private static final String EXTRA = "--";
     private static final String CRLF = "\r\n";
     private static final String QUOTE = "\"";
     private static Vector<String> svr_addr = new Vector<String>();
     private static Map<String, dt_info> id_addr = new HashMap<String, dt_info>();// 保存对应的题号的发题svr地址
     private static String str_author = "";

     /**
      * dati_type:题目类型
      * acc_str:帐号验证密码串
      * extra_str:备注字符串
      * zz:作者帐号(给予返利)
      * pri:优先级
      * timeout:超时时间
      * pic:提交图片数据  (用上传文件的方法提交)
      * 
      * @param imgdata
      * @param dati_type
      * @param timeout
      * @param pri
      * @param extra_str
      * @return
      */
     public String SendFile(byte[] imgdata, int dati_type, int timeout, int pri, String extra_str) {
          Vector<String> vAddr = new Vector<String>();
          // 获取发题线路
          // 默认的2个发题地址
          synchronized(svr_addr) {
               if(!svr_addr.isEmpty()) {
                    vAddr = svr_addr;
               }else {
                    vAddr.add("http://dt1.hyocr.com:8080");
                    vAddr.add("http://dt2.hyocr.com:8080");
               }
          }
          
          String BOUNDARY = null;
          String FILE_NAME = "; filename=";
          String CONTENT_DISPOSITION = "Content-Disposition: form-data; name=";
          HttpURLConnection conn = null;
          for(int i = 0; i < vAddr.size(); i++) {
               try {
                    BOUNDARY = "----------------" + System.currentTimeMillis();
                    String urlstr = vAddr.get(i) + "/uploadpic.php";
                    URL url = new URL(urlstr);
                    
//                    System.out.println("connect to ...." + urlstr);
                    conn = (HttpURLConnection)url.openConnection();
                    conn.setConnectTimeout(2000);// 设置超时时间
                    conn.setReadTimeout(10000);
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
//                    conn.setRequestProperty("User-Agent", "hyocr_java_http");
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
                    StringBuffer postData = new StringBuffer(EXTRA + BOUNDARY + CRLF);
                    postData.append(CONTENT_DISPOSITION + QUOTE + "pic" + QUOTE + FILE_NAME + QUOTE + (System.currentTimeMillis() + ".jpg") + QUOTE + CRLF);
                    postData.append("Content-Type: image/jpeg" + CRLF + CRLF);

                    OutputStream os = conn.getOutputStream();
                    byte headerData[] = postData.toString().getBytes();
                    os.write(headerData);

                    byte[] fileData = imgdata;
                    os.write(fileData);
                    os.write((EXTRA + BOUNDARY + CRLF).getBytes());
                    // 发送其他参数
                    String param = genernateRequestParam("acc_str", this.apicode, BOUNDARY, CONTENT_DISPOSITION) + genernateRequestParam("dati_type", String.valueOf(dati_type), BOUNDARY, CONTENT_DISPOSITION) + genernateRequestParam("timeout", Integer.toString(timeout), BOUNDARY, CONTENT_DISPOSITION) + genernateRequestParam("pri", Integer.toString(pri), BOUNDARY, CONTENT_DISPOSITION) + genernateRequestParam("extra_str", extra_str, BOUNDARY, CONTENT_DISPOSITION);

                    if(!str_author.isEmpty()) {
                         param += genernateRequestParam("zz", str_author, BOUNDARY, CONTENT_DISPOSITION);
                    }
                    os.write(param.getBytes());

                    String endS = EXTRA + BOUNDARY + EXTRA + CRLF;
                    os.write(endS.getBytes());
                    os.flush();
                    os.close();
                    String id = "";
                    InputStreamReader ir = new InputStreamReader(conn.getInputStream(), "GBK");
                    BufferedReader br = new BufferedReader(ir);
                    id = br.readLine();
                    conn.disconnect();
                    if(id != null && !id.isEmpty() && id.charAt(0) != '#') {
                         dt_info di = new dt_info();
                         di.str_svr = vAddr.get(i);
                         di.time = System.currentTimeMillis();
                         synchronized(id_addr) {
                              id_addr.put(id, di);
                         }
                    }
                    return id;
               } catch (FileNotFoundException e) {
                    return "#读取文件失败";
               } catch (MalformedURLException e) {
                    e.printStackTrace();
               } catch (IOException e) {
                    e.printStackTrace();
               } catch (Exception e) {
                    e.printStackTrace();
               }
          }
          return "#发题失败";
     }
     
     // 模拟发送表单参数。
     private String genernateRequestParam(String name, String value, String BOUNDARY, String CONTENT_DISPOSITION) {
          StringBuffer param = new StringBuffer(EXTRA + BOUNDARY + CRLF);
          param.append(CONTENT_DISPOSITION + QUOTE + name + QUOTE + CRLF);
          param.append(CRLF);
          param.append(value + CRLF);
          return param.toString();
     }
     
     public String SendGet(String urlname, String param) throws Exception {
          HttpURLConnection conn = null;
          try {
               String urlNameString = urlname;
               if(param != null && !param.isEmpty()) {
                    urlNameString = urlNameString + "?" + param;
               }
               URL realUrl = new URL(urlNameString);
               conn = (HttpURLConnection)realUrl.openConnection();
               conn.setConnectTimeout(2000);// 设置超时时间
               conn.setReadTimeout(10000);
               conn.setDoOutput(true);
               conn.setUseCaches(false);
//               conn.setRequestProperty("User-Agent", "91yzm_java_http");
               InputStreamReader ir = new InputStreamReader(conn.getInputStream(), "GBK");
               BufferedReader br = new BufferedReader(ir);
               String answer = br.readLine();
               conn.disconnect();

               return answer;
          } catch (MalformedURLException e) {
               e.printStackTrace();
               throw e;
          } catch (IOException e) {
               e.printStackTrace();
               throw e;
          }
     }
     
     public String GetAnswer(String id) {
          // 删除2分钟前的题目信息，免得占用内存, 超過2分鐘沒要到就沒用了
          long curTime = System.currentTimeMillis();
          synchronized(id_addr) {
               Iterator<String> iter = id_addr.keySet().iterator();
               while(iter.hasNext()) {
                    String key = iter.next();
                    if(curTime - id_addr.get(key).time > 2 * 60 * 1000) {
                         iter.remove();
                    }
               }
          }

          if(id.length() > 0 && id.charAt(0) == '#') {
               return id;
          }
          String str_svr = "";
          synchronized(id_addr) {
               dt_info dii = id_addr.get(id);
               if(dii != null) {
                    str_svr = dii.str_svr;
               }

          }
          String answer = "";
          if(str_svr != null && !str_svr.isEmpty()) {
               try {
                    answer = SendGet(str_svr + "/query.php", "sid=" + id);
                    if(answer != null && !answer.isEmpty()) {
                         id_addr.remove(id);// 获取到答案后就从map中删除信息以免内存占用太大
                    }
                    return answer;
               } catch (Exception e) {
               }
          }
          // 如果保存的线路无法获取答案，则从其他线路获取答案
          id_addr.remove(id);
          Vector<String> vAddr = new Vector<String>();
          synchronized(svr_addr) {
               if(!svr_addr.isEmpty()) {
                    vAddr = svr_addr;
               }else {
                    vAddr.add("http://dt2.hyocr.com:8080");
                    vAddr.add("http://dt1.hyocr.com:8080");
               }
          }
          for(int i = 0; i < vAddr.size(); i++) {
               if(str_svr == vAddr.get(i))
                    continue;
               try {
                    answer = SendGet(vAddr.get(i) + "/query.php", "sid=" + id);
                    return answer;
               } catch (Exception e) {
               }
          }
          return answer;
     }

     
     private String apicode;
     private String defaultdati;
     private int timeout;
     @Override
     public void init(Element el) throws Exception {
          super.init(el);
          this.apicode = el.getAttributeValue("apicode");
          this.defaultdati = el.getAttributeValue("defaultdati");
          this.timeout = Integer.parseInt(el.getAttributeValue("timeout"));
          String[] serverurls = el.getAttributeValue("serverurl").split(",");
          for(String serverurl: serverurls) {
               svr_addr.add(serverurl);
          }
//          System.out.println("super.getCaptchaname()=" + super.getCaptchaname() + ", super.getCaptchadesc()=" + super.getCaptchadesc() + ", this.apicode=" + this.apicode + ", this.defaultdati=" + this.defaultdati + ", timeout=" + this.timeout);
     }

     @Override
     public JSONObject analyzeImage(byte[] imgdata, String dati_type) throws Exception {
          if(dati_type == null) {
               // 如果前端不知道或無法判定帶甚麼, 給預設值
               dati_type = this.defaultdati;
          }

          JSONObject jres = new JSONObject();
          String id = SendFile(imgdata, Integer.parseInt(dati_type), this.timeout, 1, "");
          if(id.charAt(0)=='#') {
               // 失敗
               jres.put("rc", "500");
               jres.put("rm", id);
               super.stopUse();
               return jres;
          }
          jres.put("id", id);
          
          String answer = "";
          while(true) {
               try {
                    Thread.sleep(1000);
               } catch (InterruptedException e) {
                    e.printStackTrace();
               }
               answer = GetAnswer(id);
               if(!(answer == null || answer.length() == 0)) {
                    break;
               }
          }
          
          if(answer.charAt(0)=='#') {
               // 失敗
               jres.put("rc", "500");
               jres.put("rm", answer);
               super.stopUse();
          }else {
               jres.put("rc", "0");
               jres.put("text", answer);
               
          }
          return jres;
     }

     @Override
     public String report(String id) throws Exception {
          // 不支援回報
          return null;
     }

     @Override
     public String querybalance() {
          // 查詢餘額
          return "解析器:" + this.getCaptchaname() + "...尚未實作";
     }
}

class dt_info {
     String str_svr;
     long time;
}
