package captcha.fateadm;

/**
 * @author M910t
 * 婓婓打碼
 */
public class Captcha {
     private String app_id, app_key, pd_id, pd_key;
     public Captcha(String app_id, String app_key, String pd_id, String pd_key) {
          this.app_id = app_id;
          this.app_key = app_key;
          this.pd_id = pd_id;
          this.pd_key = pd_key;
     }
     
     public double queryBalance() throws Exception{
          Api api = new Api();
          // 对象生成之后，在任何操作之前，需要先调用初始化接口
          api.Init(this.app_id, this.app_key, this.pd_id, this.pd_key);

          // 查询余额
//          Util.HttpResp resp = api.QueryBalc();   // 查询余额返回详细信息
//          System.out.printf("query balc!ret: %d cust: %f err: %s reqid: %s pred: %s\n", resp.ret_code, resp.cust_val, resp.err_msg, resp.req_id, resp.pred_resl);
          
          double balance = api.QueryBalcExtend();    // 直接返回余额结果
          System.out.println("Balance:" + balance); 
          return balance;
     }
     
     public String analyze(String pred_type, byte[] imgdata) throws Exception{
          Api api = new Api();
          // 对象生成之后，在任何操作之前，需要先调用初始化接口
          api.Init(this.app_id, this.app_key, this.pd_id, this.pd_key);
          
          // 多网站类型时，需要增加src_url参数，具体请参考api文档: http://docs.fateadm.com/web/#/1?page_id=6
          Util.HttpResp resp = api.Predict(pred_type, imgdata);  // 返回识别结果的详细信息
          System.out.printf("predict from file!ret: %d cust: %f err: %s reqid: %s pred: %s\n", resp.ret_code, resp.cust_val, resp.err_msg, resp.req_id, resp.pred_resl);
          return resp.pred_resl;
     }
     
     public void report(String req_id) throws Exception{
          Api api = new Api();
          // 对象生成之后，在任何操作之前，需要先调用初始化接口
          api.Init(this.app_id, this.app_key, this.pd_id, this.pd_key);

          int ret = api.JusticeExtend(req_id); // 直接返回是否成功
          if (ret == 0 ) {
               System.out.println("Justice Success!");
          }else {
               System.out.println("Justice fail! code:" + ret);
          }
          
          Util.HttpResp resp = api.Justice(req_id); // 返回完整信息
          System.out.printf("justice !ret: %d cust: %f err: %s reqid: %s pred: %s\n", resp.ret_code, resp.cust_val, resp.err_msg, resp.req_id, resp.pred_resl);
     }
     
     public static void main( String[] args) throws Exception {
     }
     
     
}
