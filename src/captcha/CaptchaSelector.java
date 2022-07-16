package captcha;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jdom.Element;

/**
 * @author M910t
 * 可容許多個分析器，由本物件控制用哪個分析器處理，當回報分析器有問題時自動停止1分鐘不使用
 */
public class CaptchaSelector {
     private static ArrayList<CaptchaImp> capts = new ArrayList<>(); // 依權重放置解析器個數, 例如 cy權重是2就放2個
     public static synchronized void init(Element el) throws Exception{
          HashSet<String> captnames = new HashSet<>(); // 存放讀取設定的解析器名稱, 用於將已移除的設定從記憶體執行物件中移除
          
          List<Element> ls = el.getChildren();
          for(Element elc : ls) {
               String capt_name = elc.getName();
               captnames.add(capt_name);
               
               ArrayList<CaptchaImp> tmplist = (ArrayList)capts.stream().filter(c -> c.getCaptchaname().equals(capt_name)).collect(Collectors.toList());
               if(tmplist.size()==0) {
                    // 一個都沒有, 新增的
                    CaptchaImp ci = (CaptchaImp)Class.forName("captcha." + capt_name + ".Captcha").newInstance();
                    ci.init(elc);
                    for(int i = 0; i < ci.getWeigth(); i++) {
                         // 依權重放幾個, 後面只要依序抓取就好
                         capts.add(ci);
                    }
               }else {
                    // 至少有一個, 重載設定
                    CaptchaImp ci = tmplist.get(0);
                    ci.init(elc); //其實list裡面只是次數, 相同名稱的都是同一個物件
                    int count = tmplist.size();
                    if(ci.getWeigth()>count) {
                         // 新的權重比原來多, 增加
                         for(int i=0;i<(ci.getWeigth() - count);i++) {
                              capts.add(ci);
                         }
                    }else if(ci.getWeigth()<count) {
                         // 新的權重比原來少, 移除
                         for(int i=count-1;i>=0;i--) {
                              if(capts.get(i).getCaptchaname().equals(ci.getCaptchaname())) {
                                   capts.remove(i);
                              }
                         }
                    }else {
                         // 一樣, 不動作
                    }
               }
          }
          
          // 移除在設定中不存在的解析器
          for(int i=capts.size()-1;i>=0;i--) {
               CaptchaImp ci = capts.get(i);
               if(!captnames.contains(ci.getCaptchaname())) {
                    capts.remove(i);
               }
          }
     }
     
     public static synchronized CaptchaImp getCaptcha() {
          while(true) {
               CaptchaImp ci = capts.remove(0); // 從第一個取
               capts.add(ci); // 放到最後一個
               if(ci.isActive()) {
                    return ci;
               }
          }
     }
     
     public static CaptchaImp getReportCaptcha(String captname) {
          Optional<CaptchaImp> opt = capts.stream().filter(c -> c.getCaptchaname().equals(captname)).findAny();
          if(opt.isPresent()) {
               return opt.get();
          }else {
               return null;
          }
     }
}
