package api.common;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.json.JSONObject;

import captcha.CaptchaImp;
import captcha.CaptchaSelector;
import tools.Tools;

public class PicToValue {
	public  long lastretrievetime;

	public PicToValue() {
		new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(60 * 1000);
				} catch (InterruptedException ex) {
				}

			}
		}).start();

		try {
			if (System.currentTimeMillis() - lastretrievetime > 10 * 60 * 1000) {
				// 超過10分鐘才更新設定
				String captchasetting = "<captcha>\r\n"
//					+ "   <hy weight=\"1\" desc=\"火眼答题\" apicode=\"K9Ukky39Hy2vYoTM\" timeout=\"30\" defaultdati=\"88\" serverurl=\"http://dt1.hyocr.com:8080,http://dt2.hyocr.com:8080\" />\r\n"
						+ "   <cap91 weight=\"2\" desc=\"91打码\" apicode=\"g1yGuFeZOPdxZvlf\" timeout=\"30\" defaultdati=\"1007\" serverurl=\"http://dt1.91yzm.com:8080,http://dt2.91yzm.com:8080\" />\r\n"
//					+ "   <cjy weight=\"5\" desc=\"超级鹰\" username=\"testagent2020\" password=\"@Abcd1234\" timeout=\"30\" defaultcodetype=\"1902\" softid=\"910349\" softkey=\"b5e439d97e34fa68c502a61b81d2fc07\" />\r\n"
//					+ "   <deathbycaptcha weight=\"2\" desc=\"deathbycaptcha\" username=\"testagent2020\" password=\"@Abcd1234\" />\r\n"
						+ "</captcha>";
				try {
					Document doc = new SAXBuilder().build(
							new InputStreamReader(new ByteArrayInputStream(captchasetting.getBytes("utf-8")), "utf-8"));
					Element root = doc.getRootElement();
					CaptchaSelector.init(root);
				} catch (Throwable ex) {
					System.out.println(ex);
				}
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public JSONObject getDecodedValue(String picPath) {
		JSONObject jres = new JSONObject();
		try {
			CaptchaImp ci = CaptchaSelector.getCaptcha();
			File myDir = new File(picPath);
			JSONObject jcres = ci.analyzeImage(Tools.StringToByte(Tools.ByteToHexString(FileUtils.readFileToByteArray(myDir))), null);
			if (jcres.optString("rc").equals("0")) {
				jres.put("rc", jcres.optString("rc"));
				jres.put("imgid", jcres.optString("id"));
				jres.put("text", jcres.optString("text"));
			}else 
			{
				jres.put("rc", jcres.optString("rc"));
				jres.put("imgid", jcres.optString("id"));
				jres.put("text", "verify failed please type currect verify code by yourself");
			}
			return jres;
		} catch (Exception e) {
			e.printStackTrace();
			return jres;
		}
	}
}
