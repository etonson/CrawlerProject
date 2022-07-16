package exe;

import org.json.JSONArray;
import org.json.JSONObject;

import to.excel.JsonArrayToExcel;

public class TestToExcel {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JSONObject obj;
		JSONArray ja = new JSONArray();
		obj = new JSONObject();
		obj.put("序號", "1");
		obj.put("交易序號", "1");
		obj.put("服務代號", "1");
		obj.put("店號", "1");
		obj.put("機號", "1");
		obj.put("POS交易時間", "1");
		obj.put("代收條碼", "1");
		obj.put("金額", "金額");
		obj.put("建檔時間", "建檔時間");
		ja.put(obj);
		obj = new JSONObject();
		obj.put("序號", "2");
		obj.put("交易序號", "2");
		obj.put("服務代號", "2");
		obj.put("店號", "2");
		obj.put("機號", "2");
		obj.put("POS交易時間", "2");
		obj.put("代收條碼", "2");
		obj.put("金額", "金額");
		obj.put("建檔時間", "建檔時間");
		ja.put(obj);

		JsonArrayToExcel exethis = new JsonArrayToExcel("okphone", "okphone");
		exethis.doExcelFromOkPhone(ja);
	}

}
