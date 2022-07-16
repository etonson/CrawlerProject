package ok.mmk;

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
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import api.common.PicToValue;
import to.excel.JsonArrayToExcel;

public class SeleniumMMKPhone {
	// ok mmk後臺
//	private static String picPath = "/home/sixson/eclipse-workspace/okmmk.png";
//	private static String geckoLocal = "/usr/bin/geckodriver";
	private static String geckoLocal = "D:\\javaLib\\selenium-java-3.10.0\\Firefox\\geckodriver.exe";
	private static String picPath = "D:\\javaLib\\selenium-java-3.10.0\\Firefox\\okmmk.png";
	private static String baseURL = "http://61.31.200.19/okmart/service/login.php";
	private static String account = "";
	private static String pwd = "";
	private static int period = 0; // 即日起7天資料
	private static JSONObject obj = null;
	
	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("Start");
		System.setProperty("webdriver.gecko.driver", geckoLocal);
		WebDriver driver = new FirefoxDriver();
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        //登入頁面
		driver.get(baseURL);
		WebElement user_name = driver.findElement(By.name("user_name"));
		JavascriptExecutor javascriptExecutor01 = (JavascriptExecutor) driver;
		javascriptExecutor01.executeScript("arguments[0].value='" + account + "';", user_name);

		WebElement pass_word = driver.findElement(By.name("pass_word"));
		JavascriptExecutor javascriptExecutor02 = (JavascriptExecutor) driver;
		javascriptExecutor02.executeScript("arguments[0].value='" + pwd + "';", pass_word);

		WebElement siimage = driver.findElement(By.xpath("//*[@id=\"imgCode\"]"));

		// 驗證碼圖片截圖
		java.io.File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		BufferedImage fullImg = ImageIO.read(screenshot);
		int siimageWidth = siimage.getSize().getWidth();
		int siimageHeight = siimage.getSize().getHeight();
		Point point = siimage.getLocation();
		BufferedImage siimageImg = fullImg.getSubimage(point.getX(), point.getY(), siimageWidth, siimageHeight);
		ImageIO.write(siimageImg, "png", screenshot);
		File screenshotLocation = new File(picPath);
		FileUtils.copyFile(screenshot, screenshotLocation);
		// 驗證圖片解碼
		PicToValue pic = new PicToValue();
		JSONObject picObj = null;
		picObj = pic.getDecodedValue(picPath);
		// 10秒解碼時間
		Thread.sleep(10000);
		WebElement chk_code = driver.findElement(By.name("chk_code"));
		JavascriptExecutor javascriptExecutor03 = (JavascriptExecutor) driver;
		javascriptExecutor03.executeScript("arguments[0].value='" + picObj.get("text").toString().toUpperCase() + "';",
				chk_code);

		// 給一分鐘確認解碼是否有誤
		Thread.sleep(20000);
		WebElement clickable = driver.findElement(By.name("login"));
		new Actions(driver).click(clickable).perform();
		Thread.sleep(5000);
		// 進入第二頁
		driver.switchTo().frame("menu");

		Thread.sleep(5000);
		System.out.println("2STEP");
		WebElement orderManage = driver.findElement(By.linkText("廠商訂單管理"));
		System.out.println(orderManage.toString());
		new Actions(driver).click(orderManage).perform();

		driver.switchTo().defaultContent();
		Thread.sleep(3000);
		driver.switchTo().frame("main");
		wait.until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("/html/body/form/table[1]/tbody/tr[2]/td/input[7]")));
		// 選擇資料查詢範圍
		Thread.sleep(3000);
		LocalDate day = LocalDate.now();
		String minDay = day.minusDays(period).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		WebElement datemin = driver.findElement(By.name("key_order_date"));
		datemin.sendKeys(minDay);

		String maxDay = day.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		WebElement datemax = driver.findElement(By.name("key_end_date"));
		datemax.sendKeys(maxDay);
		WebElement chickSearch = driver.findElement(By.xpath("/html/body/form/table[1]/tbody/tr[2]/td/input[7]"));
		new Actions(driver).click(chickSearch).perform();
		Thread.sleep(10000);

		// 查詢有效頁數
		Select dropdown = new Select(driver.findElement(By.name("page")));
		List<WebElement> webList = dropdown.getOptions();

		int orderTableTotal;
		JSONArray requestArray = null;
		requestArray = new JSONArray();
		System.out.println("共計:" + webList.size() + "頁");
		for (int j = 0; j < webList.size(); j++) {
			// 避免刷頁後DOM位置更新,引發"StaleElementReferenceException"故重新初始化並等5秒
			Select reDropdown = new Select(driver.findElement(By.name("page")));
			Thread.sleep(5000);
			try {
				reDropdown.selectByIndex(j);
				System.out.println((j + 1) + "頁");
				Thread.sleep(10000);
				orderTableTotal = getTableRow(driver, wait);
				System.out.println("共計:" + (orderTableTotal - 1) + "列資料");
				for (int i = 2; i <= orderTableTotal + 1; i++) {
					try {
						obj = new JSONObject();
						String rowI = String.valueOf(i);
						System.out.println("第:" + (i - 1) + "列資料");
						WebElement getTable = driver
								.findElement(By.xpath("/html/body/form/table[3]/tbody/tr[" + rowI + "]/td[1]"));
						obj.put("交易日期", getTable.getText().toString());
						getTable = driver
								.findElement(By.xpath("/html/body/form/table[3]/tbody/tr[" + rowI + "]/td[2]"));
						obj.put("交易序號", getTable.getText().toString());
						getTable = driver
								.findElement(By.xpath("/html/body/form/table[3]/tbody/tr[" + rowI + "]/td[3]"));
						obj.put("服務類別名稱", getTable.getText().toString());
						getTable = driver
								.findElement(By.xpath("/html/body/form/table[3]/tbody/tr[" + rowI + "]/td[4]"));
						obj.put("服務項目代號", getTable.getText().toString());
						getTable = driver
								.findElement(By.xpath("/html/body/form/table[3]/tbody/tr[" + rowI + "]/td[5]"));
						obj.put("店舖編號/店舖名稱", getTable.getText().toString());
						getTable = driver
								.findElement(By.xpath("/html/body/form/table[3]/tbody/tr[" + rowI + "]/td[6]"));
						obj.put("數量", getTable.getText());
						getTable = driver
								.findElement(By.xpath("/html/body/form/table[3]/tbody/tr[" + rowI + "]/td[7]"));
						obj.put("點數", getTable.getText().toString());
						getTable = driver
								.findElement(By.xpath("/html/body/form/table[3]/tbody/tr[" + rowI + "]/td[8]"));
						obj.put("金額", getTable.getText().toString());
						getTable = driver
								.findElement(By.xpath("/html/body/form/table[3]/tbody/tr[" + rowI + "]/td[9]"));
						obj.put("繳費門市", getTable.getText().toString());
						
						getSecondPageData(driver, wait,String.valueOf(i));
						requestArray.put(obj);
						System.out.println(obj.toString());
					} catch (NoSuchElementException e) {
						System.out.println(e.getRawMessage());
					}
				}
			} catch (NoSuchElementException e) {
				System.out.println(e.getRawMessage());
				System.out.println("資料抓取結束");
			} catch (StaleElementReferenceException e) {
				e.printStackTrace();
				System.out.println(driver.toString());
				System.out.println("資料抓取異常");
				break;
			}
		}
		System.out.println("共計:"+requestArray.length()+"筆資料");
		
		driver.close();
		JsonArrayToExcel exethis = new JsonArrayToExcel("okMMK", "okMMK");
		exethis.doExcelFromOkMMK(requestArray);
	}
	private static void getSecondPageData(WebDriver driver, WebDriverWait wait,String hrefStr) throws InterruptedException {
		String winHandleBefore = driver.getWindowHandle();
		WebElement liftLink = driver.findElement(By.xpath("/html/body/form/table[3]/tbody/tr["+hrefStr+"]/td[10]/a/img"));
		((JavascriptExecutor) driver).executeScript("arguments[0].click()", liftLink);

		Thread.sleep(2000);
		for (String winHandle : driver.getWindowHandles()) {
			driver.switchTo().window(winHandle);
		}
		Thread.sleep(1000);
		driver.switchTo().defaultContent();
		driver.switchTo().frame("main");
		System.out.println(driver.toString());
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/form/fieldset[2]/table/tbody/tr[2]/td[3]")));
		WebElement getTable = driver.findElement(By.xpath("/html/body/form/fieldset[2]/table/tbody/tr[2]/td[3]"));
		obj.put("條碼", getTable.getText().toString());

		driver.navigate().back();

		driver.switchTo().window(winHandleBefore);
		Thread.sleep(1000);
		driver.switchTo().defaultContent();
		driver.switchTo().frame("main");

		wait.until(ExpectedConditions.presenceOfElementLocated(By.name("page")));
	}
	private static int getTableRow(WebDriver driver, WebDriverWait wait) {
		wait.until(ExpectedConditions.presenceOfElementLocated((By.xpath("/html/body/form/table[3]"))));
		WebElement tableDeal = driver.findElement(By.xpath("/html/body/form/table[3]"));
		List<WebElement> webList = tableDeal.findElements(By.tagName("tr"));
		return webList.size();
	}
}
