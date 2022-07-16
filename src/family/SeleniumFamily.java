package family;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import api.common.PicToValue;
import to.excel.JsonArrayToExcel;

public class SeleniumFamily {
//	private static String geckoLocal = "/usr/bin/geckodriver";
//	private static String picPath = "/home/sixson/eclipse-workspace/family.png";
	private static String geckoLocal = "D:\\javaLib\\selenium-java-3.10.0\\Firefox\\geckodriver.exe";
    private static String picPath = "D:\\javaLib\\selenium-java-3.10.0\\Firefox\\family.png";
	private static int period = 0; // 即日起7天資料
	private static String baseURL = "https://ecb.famiport.com.tw/familyec/login.aspx";
	private static String account = "";
	private static String pwd = "";
	private static JSONObject obj = null;

	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("Start");
		System.setProperty("webdriver.gecko.driver", geckoLocal);
		WebDriver driver = new FirefoxDriver();
		driver.manage().window().maximize();
		driver.get(baseURL);
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));

		logIn(driver, wait);

		// ----第二頁----//
		// 切IFRAME-LIFT
		Thread.sleep(5000);

		driver.switchTo().frame("left");
		Thread.sleep(1000);
		
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.cssSelector("#form1 > ol:nth-child(2) > li:nth-child(1) > span:nth-child(1) > img:nth-child(1)")));
		WebElement liftList = driver.findElement((By.cssSelector("#form1 > ol:nth-child(2) > li:nth-child(1) > span:nth-child(1) > img:nth-child(1)")));
		JavascriptExecutor executor  = (JavascriptExecutor)driver;
		executor.executeScript("arguments[0].click();", liftList);
		
		Thread.sleep(1000);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("交易查詢")));
		WebElement liftLink = driver.findElement(By.linkText("交易查詢"));
		new Actions(driver).click(liftLink).perform();

		// 切IFRAME-MainFrame
		driver.switchTo().defaultContent();
		Thread.sleep(3000);
		driver.switchTo().frame("MainFrame");

		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("btnSearch")));
		LocalDate day = LocalDate.now();

		String minDay = day.minusDays(period).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		WebElement datemin = driver.findElement(By.name("txtRec_DateS"));
		JavascriptExecutor javascriptExecutor04 = (JavascriptExecutor) driver;
		javascriptExecutor04.executeScript("arguments[0].value='" + minDay + "';", datemin);

		String maxDay = day.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		WebElement datemax = driver.findElement(By.name("txtRec_DateE"));
		JavascriptExecutor javascriptExecutor05 = (JavascriptExecutor) driver;
		javascriptExecutor05.executeScript("arguments[0].value='" + maxDay + "';", datemax);
		Thread.sleep(7000);
		
		Thread.sleep(60000);
		WebElement getSearch = driver.findElement(By.id("btnSearch"));
		new Actions(driver).click(getSearch).perform();

//-------------------------------------------------------繳款取值

		JSONArray requestPayArray = new JSONArray();
		int pageTotal = getPageTotalLabel(driver, wait);
		int tableTotal;
		System.out.println("共計" + pageTotal + "頁");
		for (int j = 1; j <= pageTotal; j++) {
			System.out.println("第" + j + "頁");
			try {
				tableTotal = getTableRow(driver, wait);
				System.out.println("共計:" + (tableTotal - 1) + "列資料");
				for (int i = 2; i <= tableTotal + 1; i++) {
					try {
						obj = new JSONObject();
						String rowI = String.valueOf(i);
						System.out.println("第:" + (i - 1) + "列資料");
						WebElement getTable = driver.findElement(By.xpath(
								"/html/body/form/div[3]/table/tbody/tr[1]/td/div/table/tbody/tr[" + rowI + "]/td[2]"));
						obj.put("代收代號", getTable.getText().toString());

						getTable = driver.findElement(By.xpath(
								"/html/body/form/div[3]/table/tbody/tr[1]/td/div/table/tbody/tr[" + rowI + "]/td[3]"));
						obj.put("店舖代號", getTable.getText().toString());

						getTable = driver.findElement(By.xpath(
								"/html/body/form/div[3]/table/tbody/tr[1]/td/div/table/tbody/tr[" + rowI + "]/td[4]"));
						obj.put("店舖名稱", getTable.getText().toString());

						getTable = driver.findElement(By.xpath(
								"/html/body/form/div[3]/table/tbody/tr[1]/td/div/table/tbody/tr[" + rowI + "]/td[5]"));
						obj.put("訂單日期", getTable.getText().toString());

						getTable = driver.findElement(By.xpath(
								"/html/body/form/div[3]/table/tbody/tr[1]/td/div/table/tbody/tr[" + rowI + "]/td[6]"));
						obj.put("繳款日期", getTable.getText().toString());

						getTable = driver.findElement(By.xpath(
								"/html/body/form/div[3]/table/tbody/tr[1]/td/div/table/tbody/tr[" + rowI + "]/td[7]"));
						obj.put("交易序號", getTable.getText());

						getTable = driver.findElement(By.xpath(
								"/html/body/form/div[3]/table/tbody/tr[1]/td/div/table/tbody/tr[" + rowI + "]/td[8]"));
						obj.put("廠商訂單編號", getTable.getText().toString());

						getTable = driver.findElement(By.xpath(
								"/html/body/form/div[3]/table/tbody/tr[1]/td/div/table/tbody/tr[" + rowI + "]/td[9]"));
						obj.put("第二段條碼", getTable.getText().toString());

						getTable = driver.findElement(By.xpath(
								"/html/body/form/div[3]/table/tbody/tr[1]/td/div/table/tbody/tr[" + rowI + "]/td[10]"));
						obj.put("廠商名稱", getTable.getText().toString());

						getTable = driver.findElement(By.xpath(
								"/html/body/form/div[3]/table/tbody/tr[1]/td/div/table/tbody/tr[" + rowI + "]/td[11]"));
						obj.put("代收金額", getTable.getText().toString());

						getTable = driver.findElement(By.xpath(
								"/html/body/form/div[3]/table/tbody/tr[1]/td/div/table/tbody/tr[" + rowI + "]/td[12]"));
						obj.put("回覆狀態", getTable.getText().toString());

						getTable = driver.findElement(By.xpath(
								"/html/body/form/div[3]/table/tbody/tr[1]/td/div/table/tbody/tr[" + rowI + "]/td[13]"));
						obj.put("交易狀態", getTable.getText().toString());

						getTable = driver.findElement(By.xpath(
								"/html/body/form/div[3]/table/tbody/tr[1]/td/div/table/tbody/tr[" + rowI + "]/td[14]"));
						obj.put("資料狀態", getTable.getText().toString());

						getTable = driver.findElement(By.xpath(
								"/html/body/form/div[3]/table/tbody/tr[1]/td/div/table/tbody/tr[" + rowI + "]/td[15]"));
						obj.put("狀態", getTable.getText().toString());
						getSecondPageData(driver,wait);
						requestPayArray.put(obj);
						System.out.println(obj.toString());
						obj = null;
					} catch (NoSuchElementException e) {
						System.out.println(e.getRawMessage());
						nextPage(driver, wait);
						Thread.sleep(5000);
					}
				}
			} catch (NoSuchElementException e) {
				System.out.println(e.getRawMessage());
				System.out.println("資料抓取結束");
				System.out.println("繳款取值:"+requestPayArray.length()+"筆");
			}
		}
		JsonArrayToExcel exethis= new JsonArrayToExcel("Farmily繳費", "Farmily");
		exethis.doExcelFromFamily(requestPayArray);
//		driver.close();


	}

	private static void logIn(WebDriver driver, WebDriverWait wait) throws InterruptedException {
		WebElement txtCus1 = driver.findElement(By.name("txtCus1"));
		JavascriptExecutor javascriptExecutor01 = (JavascriptExecutor) driver;
		javascriptExecutor01.executeScript("arguments[0].value='" + account + "';", txtCus1);

		WebElement txtCus2 = driver.findElement(By.name("txtCus2"));
		JavascriptExecutor javascriptExecutor02 = (JavascriptExecutor) driver;
		javascriptExecutor02.executeScript("arguments[0].value='" + pwd + "';", txtCus2);

		// 驗證照片截圖
		try {
			WebElement siimage = driver.findElement(By.xpath("//*[@id=\"TBValidateCode1\"]"));
			java.io.File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			BufferedImage fullImg = ImageIO.read(screenshot);
			int siimageWidth = siimage.getSize().getWidth();
			int siimageHeight = siimage.getSize().getHeight();
			Point point = siimage.getLocation();
			BufferedImage siimageImg = fullImg.getSubimage(point.getX(), point.getY(), siimageWidth, siimageHeight);
			ImageIO.write(siimageImg, "png", screenshot);
			File screenshotLocation = new File(picPath);
			FileUtils.copyFile(screenshot, screenshotLocation);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Thread.sleep(3000);

		PicToValue pic = new PicToValue();
		JSONObject picObj = pic.getDecodedValue(picPath);
		//10秒解碼時間
		Thread.sleep(10000);

		WebElement reTxtChkAut = driver.findElement(By.name("txtChkAut"));
		JavascriptExecutor javascriptExecutor03 = (JavascriptExecutor) driver;
		javascriptExecutor03.executeScript("arguments[0].value='"+picObj.get("text").toString()+"';", reTxtChkAut);

		//給一分鐘確認解碼是否有誤
		Thread.sleep(60000);
		WebElement clickable = driver.findElement(By.id("btnLogin"));
		new Actions(driver).click(clickable).perform();
	}
	
	private static void getSecondPageData(WebDriver driver, WebDriverWait wait) throws InterruptedException {
		String winHandleBefore = driver.getWindowHandle();
		WebElement liftLink = driver.findElement(By.linkText(obj.getString("第二段條碼")));
		((JavascriptExecutor) driver).executeScript("arguments[0].click()", liftLink);

		Thread.sleep(5000);
		for (String winHandle : driver.getWindowHandles()) {
			driver.switchTo().window(winHandle);
		}

		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"txt_PIN_CODE\"]")));
		WebElement getTable = driver.findElement(By.xpath("//*[@id=\"txt_PIN_CODE\"]"));
		obj.put("PIN_CODE", getTable.getText().toString());
		getTable = driver.findElement(By.xpath("/html/body/form/table/tbody/tr[2]/td/table/tbody/tr/td/table/tbody/tr[25]/td[4]"));
		obj.put("DESC3", getTable.getText().toString());
		
		driver.close();

		driver.switchTo().window(winHandleBefore);
		Thread.sleep(3000);
		driver.switchTo().defaultContent();
		Thread.sleep(3000);
		driver.switchTo().frame("MainFrame");

		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("btnSearch")));
	}

	// 查詢下一頁工具
	private static void nextPage(WebDriver driver, WebDriverWait wait) {
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("btnNextPage")));
		WebElement btnNextPage = driver.findElement(By.id("btnNextPage"));
		new Actions(driver).click(btnNextPage).perform();
	}

	private static int getTableRow(WebDriver driver, WebDriverWait wait) {
		wait.until(ExpectedConditions.presenceOfElementLocated((By.id("GridView1"))));
		WebElement tableDeal = driver.findElement(By.id("GridView1"));
		List<WebElement> webList = tableDeal.findElements(By.tagName("tr"));
		return webList.size();
	}

	private static int getPageTotalLabel(WebDriver driver, WebDriverWait wait) {
		wait.until(ExpectedConditions.presenceOfElementLocated((By.id("PageTotalLabel"))));
		WebElement PageTotalLabel = driver.findElement(By.id("PageTotalLabel"));
		String dataPage = PageTotalLabel.getText();
		int dataPageInt = Integer.parseInt(dataPage);
		return dataPageInt;
	}
}
