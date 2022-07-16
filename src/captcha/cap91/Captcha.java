package captcha.cap91;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.jdom.Element;
import org.json.JSONObject;

import captcha.CaptchaImp;

/**
 * @author M910t
 * 91打碼 OK
 * 
 * http://www.91yzm.com/
 * testagent2020 / @Abcd1234
 */
public class Captcha extends CaptchaImp{
     private static final String EXTRA = "--";
     private static final String CRLF = "\r\n";
     private static final String QUOTE = "\"";
     private static final String str_query_svr = "http://plugin.config.91yzm.com:8080/apisvrs.php?ver=20";
     private static String str_author = "";
     private static Vector<String> svr_addr = new Vector<String>();
     private static Map<String, dt_info> id_addr = new HashMap<String, dt_info>();// 保存对应的题号的发题svr地址
     private static long preQueryTime = 0;
     
     private String apicode = "g1yGuFeZOPdxZvlf"; // 串接密碼, 帐号验证密码串
     private int timeout; //處理逾時(sec)
     private String defaultdati; //預設的驗證碼類型

     public static void SetAuthor(String author) {
          str_author = author;
     }

     public void GetSvrAddr() {
          long curTime = System.currentTimeMillis();

          String data = "";
          // 5分钟更新次线路
          if(curTime > preQueryTime + 5 * 60 * 1000) {
               preQueryTime = curTime;
               try {
                    data = SendGet(str_query_svr, "");
               } catch (Exception e) {

               }
          }
          if(data != null && !data.isEmpty()) {
               // 解析或得到的服务器地址
               Vector<String> vAddr = new Vector<String>();
               int pre_pos = 0;
               while(true) {
                    int pos = data.indexOf(";");
                    if(pos != -1) {
                         vAddr.add(data.substring(pre_pos, pos));
                         data = data.substring(pos + 1);
                    } else {
                         if(!data.isEmpty())
                              vAddr.add(data);
                         break;
                    }
               }

               synchronized(svr_addr) {
                    svr_addr = vAddr;
               }
          }
     }

     /**
      * @param imgdata 圖片資料
      * @param dati_type 题目类型
      * @param timeout 超时时间
      * @param pri 优先级
      * @param extra_str 备注字符串
      * @return
      */
     public String SendFile(byte[] imgdata, int dati_type, int timeout, int pri, String extra_str) {
          GetSvrAddr();
          Vector<String> vAddr = new Vector<String>();
          // 获取发题线路
          // 默认的2个发题地址
          synchronized(svr_addr) {
               if(!svr_addr.isEmpty()) {
                    vAddr = svr_addr;
               }else {
                    vAddr.add("http://dt1.91yzm.com:8080");
                    vAddr.add("http://dt2.91yzm.com:8080");
               }
          }
          
          HttpURLConnection conn = null;
          String BOUNDARY = null;
          String FILE_NAME = "; filename=";
          String CONTENT_DISPOSITION = "Content-Disposition: form-data; name=";
          
          // 逐个发题，遇到成功的即可返回
          for(int i = 0; i < vAddr.size(); i++) {
               try {
                    BOUNDARY = "----------------" + System.currentTimeMillis();
                    URL url;
                    url = new URL(vAddr.get(i) + "/uploadpic.php");
                    conn = (HttpURLConnection)url.openConnection();
                    conn.setConnectTimeout(2000);// 设置超时时间
                    conn.setReadTimeout(10000);
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setRequestProperty("User-Agent", "91yzm_java_http");
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
                    StringBuffer postData = new StringBuffer(EXTRA + BOUNDARY + CRLF);
                    postData.append(CONTENT_DISPOSITION + QUOTE + "pic" + QUOTE + FILE_NAME + QUOTE + (System.currentTimeMillis() + ".jpg") + QUOTE + CRLF);
                    postData.append("Content-Type: image/jpeg" + CRLF + CRLF);

                    OutputStream os = conn.getOutputStream();
                    byte headerData[] = postData.toString().getBytes();
                    os.write(headerData);

//                    InputStream is = new ByteArrayInputStream(imgdata);
//                    byte[] fileData = getBytes(is);
                    byte[] fileData = imgdata;
                    os.write(fileData);
                    os.write((EXTRA + BOUNDARY + CRLF).getBytes());
                    // 发送其他参数
                    String param = genernateRequestParam("acc_str", this.apicode, BOUNDARY, CONTENT_DISPOSITION) + genernateRequestParam("dati_type", Integer.toString(dati_type), BOUNDARY, CONTENT_DISPOSITION) + genernateRequestParam("timeout", Integer.toString(timeout), BOUNDARY, CONTENT_DISPOSITION) + genernateRequestParam("pri", Integer.toString(pri), BOUNDARY, CONTENT_DISPOSITION) + genernateRequestParam("extra_str", extra_str, BOUNDARY, CONTENT_DISPOSITION);
                    if(!str_author.isEmpty())
                         param += genernateRequestParam("zz", str_author, BOUNDARY, CONTENT_DISPOSITION);
                    os.write(param.getBytes());

                    String endS = EXTRA + BOUNDARY + EXTRA + CRLF;
                    os.write(endS.getBytes());
                    os.flush();
                    os.close();
                    String id = "";
                    InputStreamReader ir = new InputStreamReader(conn.getInputStream());
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

     public String SendGet(String urlname, String param) throws Exception {
          HttpURLConnection conn = null;
          try {
               String urlNameString = urlname;
               if(param != null && !param.isEmpty())
                    urlNameString = urlNameString + "?" + param;
               URL realUrl = new URL(urlNameString);
               conn = (HttpURLConnection)realUrl.openConnection();
               conn.setConnectTimeout(2000);// 设置超时时间
               conn.setReadTimeout(10000);
               conn.setDoOutput(true);
               conn.setUseCaches(false);
               conn.setRequestProperty("User-Agent", "91yzm_java_http");
               InputStreamReader ir = new InputStreamReader(conn.getInputStream(), "GBK");
               BufferedReader br = new BufferedReader(ir);
               String answer = "";
               answer = br.readLine();
               conn.disconnect();

               return answer;
               // return br.readLine();
          } catch (MalformedURLException e) {
               e.printStackTrace();
               throw e;
          } catch (IOException e) {
               e.printStackTrace();
               throw e;
          }
     }

     public String GetAnswer(String id) {
          // 删除5分钟前的题目信息，免得占用内存
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
                    vAddr.add("http://dt1.91yzm.com:8080");
                    vAddr.add("http://dt2.91yzm.com:8080");
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


     // 模拟发送表单参数。
     private String genernateRequestParam(String name, String value, String BOUNDARY, String CONTENT_DISPOSITION) {
          StringBuffer param = new StringBuffer(EXTRA + BOUNDARY + CRLF);
          param.append(CONTENT_DISPOSITION + QUOTE + name + QUOTE + CRLF);
          param.append(CRLF);
          param.append(value + CRLF);
          return param.toString();
     }

     @Override
     public void init(Element el) throws Exception {
          super.init(el);
          this.apicode = el.getAttributeValue("apicode");
          this.timeout = Integer.parseInt(el.getAttributeValue("timeout"));
          this.defaultdati = el.getAttributeValue("defaultdati");
          
          String[] serverurls = el.getAttributeValue("serverurl").split(",");
          for(String serverurl: serverurls) {
               svr_addr.add(serverurl);
          }
     }

     @Override
     public JSONObject analyzeImage(byte[] imgdata, String dati_type) throws Exception {
          if(dati_type == null) {
               // 如果前端不知道或無法判定帶甚麼, 給預設值
               dati_type = this.defaultdati;
          }

          JSONObject jres = new JSONObject();
          String id = SendFile(imgdata, Integer.parseInt(dati_type), this.timeout, 1, ""); // c91.SendFile(apicode, imgdata, 21999, 30, 1, "");
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
          // 91打碼沒有回報錯誤機制
          return null;
     }

     @Override
     public String querybalance() {
          // TODO 查詢餘額
//          return "解析器:" + this.getCaptchaname() + ", 餘額:" GetScore(this.username, this.password);
          return "解析器:" + this.getCaptchaname() + "...尚未實作";
     }
}

class dt_info {
     String str_svr;
     long time;
}
