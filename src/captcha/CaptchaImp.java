package captcha;

import org.jdom.Element;
import org.json.JSONObject;

public abstract class CaptchaImp {
     public abstract JSONObject analyzeImage(byte[] imgdata, String dati_type) throws Exception;
     public abstract String report(String id) throws Exception;
     public abstract String querybalance();

     private String captchaname;
     private String captchadesc;
     protected int weigth;
     private boolean active = true;
     
     public void init(Element el) throws Exception{
          this.captchaname = el.getName();
          this.captchadesc = el.getAttributeValue("desc");
          this.weigth = Integer.parseInt(el.getAttributeValue("weight"));
     }

     
     public boolean isActive() {
          return this.active;
     }
     public synchronized void stopUse() {
          if(this.active) {
               this.active = false;

               // 睡1分鐘後再把active打開
               new Thread(()->{
                    try {
                         Thread.sleep(60*1000);
                    } catch (InterruptedException e) {
                    }
                    this.active = true;
               }).start();
          }
     }
     public String getCaptchaname() {
          return captchaname;
     }
     public void setCaptchaname(String captchaname) {
          this.captchaname = captchaname;
     }
     public int getWeigth() {
          return weigth;
     }
     public void setWeigth(int weigth) {
          this.weigth = weigth;
     }
     public String getCaptchadesc() {
          return captchadesc;
     }
     public void setCaptchadesc(String captchadesc) {
          this.captchadesc = captchadesc;
     }
}
