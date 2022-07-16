package to.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonArrayToExcel {
	public XSSFWorkbook workbook;
	public XSSFSheet spreadsheet;
	public String fileNmae;
	public String sheetNmae;

	public JsonArrayToExcel(String fileNmae, String sheetNmae) {
		this.sheetNmae = sheetNmae;
		this.fileNmae = fileNmae;
		this.workbook = new XSSFWorkbook();
		// Create a blank sheet
		this.spreadsheet = workbook.createSheet(sheetNmae);
		// Create row object

	}

	public void doExcelFromOkPhone(JSONArray requestArray) {
		XSSFRow row;
		Map<String, Object[]> tableInfo = new LinkedHashMap<String, Object[]>();
		tableInfo.put("1", new Object[] { "序號", "交易序號", "服務代號", "店號", "機號", "POS交易時間", "代收條碼", "金額", "建檔時間" });
		try {
			for (int i = 0; i <requestArray.length(); i++) {
				JSONObject jsonObj = requestArray.getJSONObject(i);
				tableInfo.put(String.valueOf(i + 2),
						new Object[] { jsonObj.optString("序號"), jsonObj.optString("交易序號"), jsonObj.optString("服務代號"),
								jsonObj.optString("店號"), jsonObj.optString("機號"), jsonObj.optString("POS交易時間"),
								jsonObj.optString("代收條碼"), jsonObj.optString("金額"), jsonObj.optString("建檔時間") });
			}

		} catch (JSONException e) {
			System.out.println(e.getMessage());
		}
		
		Set<String> keyid = tableInfo.keySet();
		int rowid = 0;
		for (String key : keyid) {
			row = spreadsheet.createRow(rowid++);
			Object[] objectArr = tableInfo.get(key);
			int cellid = 0;
			for (Object obj : objectArr) {
				Cell cell = row.createCell(cellid++);
				cell.setCellValue((String) obj);
			}
		}
		
		try (FileOutputStream out = new FileOutputStream(
				new File("D:\\javaLib\\selenium-java-3.10.0\\Firefox\\" + fileNmae + ".xlsx"));) {
			workbook.write(out);
			System.out.println("Writesheet.xlsx written successfully");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.print("excel失敗");
		}
	}

	public void doExcelFromOkMMK(JSONArray requestArray) {
		XSSFRow row;
		Map<String, Object[]> tableInfo = new LinkedHashMap<String, Object[]>();
		tableInfo.put("1", new Object[] { "交易日期", "交易序號", "服務類別名稱", "服務項目代號", "店舖編號/店舖名稱", "數量", "點數", "金額", "繳費門市","條碼" });
		try {
			for (int i = 0; i <requestArray.length(); i++) {
				JSONObject jsonObj = requestArray.getJSONObject(i);
				tableInfo.put(String.valueOf(i + 2),
						new Object[] { jsonObj.optString("交易日期"), jsonObj.optString("交易序號"),
								jsonObj.optString("服務類別名稱"), jsonObj.optString("服務項目代號"),
								jsonObj.optString("店舖編號/店舖名稱"), jsonObj.optString("數量"), jsonObj.optString("點數"),
								jsonObj.optString("金額"), jsonObj.optString("繳費門市"), jsonObj.optString("條碼") });
			}

		} catch (JSONException e) {
			System.out.println(e.getMessage());
		}
		
		Set<String> keyid = tableInfo.keySet();
		int rowid = 0;
		for (String key : keyid) {
			row = spreadsheet.createRow(rowid++);
			Object[] objectArr = tableInfo.get(key);
			int cellid = 0;
			for (Object obj : objectArr) {
				Cell cell = row.createCell(cellid++);
				cell.setCellValue((String) obj);
			}
		}
		
		try (FileOutputStream out = new FileOutputStream(
				new File("D:\\javaLib\\selenium-java-3.10.0\\Firefox\\" + fileNmae + ".xlsx"));) {
			workbook.write(out);
			System.out.println("Writesheet.xlsx written successfully");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.print("excel失敗");
		}
	}

	public void doExcelFromFamily(JSONArray requestArray) throws InterruptedException {
		System.out.println(requestArray.toString());
		System.out.println("JSONArray to Excel!");
		XSSFRow row;
		Map<String, Object[]> tableInfo = new LinkedHashMap<String, Object[]>();
		tableInfo.put("1", new Object[] { "代收代號", "店舖代號", "店舖名稱", "訂單日期", "繳款日期", "交易序號", "廠商訂單編號", "第二段條碼", "廠商名稱",
				"代收金額", "回覆狀態", "交易狀態", "資料狀態", "狀態" , "PIN_CODE", "DESC3"});
		try {
			System.out.print("excel開始存入");
			for (int i = 0; i < requestArray.length(); i++) {
				JSONObject jsonObj = requestArray.getJSONObject(i);
				tableInfo.put(String.valueOf(i + 2),
						new Object[] { jsonObj.optString("代收代號"), jsonObj.optString("店舖代號"), jsonObj.optString("店舖名稱"),
								jsonObj.optString("訂單日期"), jsonObj.optString("繳款日期"), jsonObj.optString("交易序號"),
								jsonObj.optString("廠商訂單編號"), jsonObj.optString("第二段條碼"), jsonObj.optString("廠商名稱"),
								jsonObj.optString("代收金額"), jsonObj.optString("回覆狀態"), jsonObj.optString("交易狀態"),
								jsonObj.optString("資料狀態"), jsonObj.optString("狀態") , jsonObj.optString("PIN_CODE"), 
								jsonObj.optString("DESC3")});
			}

		} catch (JSONException e) {
			System.out.println(e.getMessage());
		}

		Set<String> keyid = tableInfo.keySet();
		int rowid = 0;
		for (String key : keyid) {
			row = spreadsheet.createRow(rowid++);
			Object[] objectArr = tableInfo.get(key);
			int cellid = 0;
			for (Object obj : objectArr) {
				Cell cell = row.createCell(cellid++);
				cell.setCellValue((String) obj);
			}
		}
		
		try (FileOutputStream out = new FileOutputStream(
				new File("D:\\javaLib\\selenium-java-3.10.0\\Firefox\\" + fileNmae + ".xlsx"));) {
			workbook.write(out);
			System.out.println("Writesheet.xlsx written successfully");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.print("excel失敗");
		}
	}

}
