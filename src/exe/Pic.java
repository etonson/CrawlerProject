package exe;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.json.JSONObject;

import captcha.CaptchaImp;
import captcha.CaptchaSelector;
import tools.Tools;

public class Pic {
	private static long lastretrievetime;
	private static HashMap<String, CaptchaReporter> crs = new HashMap<>();

	public Pic() {
		new Thread(() -> {
			ArrayList<CaptchaReporter> tmplist = new ArrayList<CaptchaReporter>();
			while (true) {
				tmplist.clear();
				try {
					Thread.sleep(60 * 1000);
				} catch (InterruptedException ex) {
				}

				Iterator<CaptchaReporter> ites = crs.values().iterator();
				while (ites.hasNext()) {
					tmplist.add(ites.next());
				}
			}
		}).start();

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

	}

	public static void main(String[] args) {
		
		new Thread(() -> {
			ArrayList<CaptchaReporter> tmplist = new ArrayList<CaptchaReporter>();
			while (true) {
				tmplist.clear();
				try {
					Thread.sleep(60 * 1000);
				} catch (InterruptedException ex) {
				}

				Iterator<CaptchaReporter> ites = crs.values().iterator();
				while (ites.hasNext()) {
					tmplist.add(ites.next());
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
		String p_imgdata = "D:\\javaLib\\selenium-java-3.10.0\\Firefox\\family.png";
		// op 有 tell_captcha、report, tell_captcha必須給圖形的base64字串, 但不加入mac; report必須給id
		JSONObject jres = new JSONObject();
		try {
			CaptchaImp ci = CaptchaSelector.getCaptcha();
			System.out.println(ci.getWeigth());
			File myDir = new File(p_imgdata);
			JSONObject jcres = ci.analyzeImage(Tools.StringToByte(Tools.ByteToHexString(FileUtils.readFileToByteArray(myDir))), null);
			System.out.println(Tools.StringToByte(Tools.ByteToHexString(FileUtils.readFileToByteArray(myDir))));
			if (jcres.optString("rc").equals("0")) {
				jres.put("imgid", jcres.optString("id"));
				jres.put("text", jcres.optString("text"));

				System.out.println(jcres.toString());
			}
		}catch(Exception e) 
		{
			e.printStackTrace();
		}
	}


}
