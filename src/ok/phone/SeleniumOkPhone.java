package ok.phone;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
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

public class SeleniumOkPhone {
//ok 手機條碼後臺
//	private static String geckoLocal = "/usr/bin/geckodriver";
//	private static String picPath = "/home/sixson/eclipse-workspace/okphone.png";
	private static String picPath = "D:\\javaLib\\selenium-java-3.10.0\\Firefox\\okphone.png";
	private static String geckoLocal = "D:\\javaLib\\selenium-java-3.10.0\\Firefox\\geckodriver.exe";
	private static String baseURL = "https://okservicemmk.okmart.com.tw/service/login.php";
	private static String account = "";
	private static String pwd = "";
	private static int period=2; //即日起7天資料

	public SeleniumOkPhone() {

	}

	public static void main(String[] args) throws IOException, InterruptedException {
		System.setProperty("webdriver.gecko.driver", geckoLocal);
		WebDriver driver = new FirefoxDriver();
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

		driver.get(baseURL);
		WebElement login_data1 = driver.findElement(By.name("login_data1"));
		JavascriptExecutor javascriptExecutor01 = (JavascriptExecutor) driver;
		javascriptExecutor01.executeScript("arguments[0].value='" + account + "';", login_data1);

		WebElement login_data2 = driver.findElement(By.name("login_data2"));
		JavascriptExecutor javascriptExecutor02 = (JavascriptExecutor) driver;
		javascriptExecutor02.executeScript("arguments[0].value='" + pwd + "';", login_data2);

		//驗證碼圖片截圖
		WebElement siimage = driver.findElement(By.xpath("//*[@id=\"siimage\"]"));
		java.io.File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		BufferedImage fullImg = ImageIO.read(screenshot);
		int siimageWidth = siimage.getSize().getWidth();
		int siimageHeight = siimage.getSize().getHeight();
		Point point = siimage.getLocation();
		BufferedImage siimageImg = fullImg.getSubimage(point.getX(), point.getY(), siimageWidth, siimageHeight);
		ImageIO.write(siimageImg, "png", screenshot);
		File screenshotLocation = new File(picPath);
		FileUtils.copyFile(screenshot, screenshotLocation);

		//驗證圖片解碼
		PicToValue pic = new PicToValue();
		JSONObject picObj = pic.getDecodedValue(picPath);
		//10秒解碼時間
		Thread.sleep(10000);
		WebElement chk_code = driver.findElement(By.name("login_data3"));
		JavascriptExecutor javascriptExecutor03 = (JavascriptExecutor) driver;
		javascriptExecutor03.executeScript("arguments[0].value='" + picObj.get("text").toString().toUpperCase() + "';",
				chk_code);

		//給一分鐘確認解碼是否有誤
		Thread.sleep(60000);
		WebElement clickable = driver.findElement(By.xpath("/html/body/div[2]/div/form/div[4]/div/input[1]"));
		new Actions(driver).click(clickable).perform();
		// ----第二頁----//
		Thread.sleep(3000);
		System.out.println("1STEP");
		WebElement divScrollValue = driver.findElement(By.id("menu_system_18"));
		System.out.println(divScrollValue.toString());
		new Actions(driver).click(divScrollValue).perform();

		System.out.println("2STEP");
		Thread.sleep(5000);
		WebElement menuList = driver.findElement(By.linkText("代收訂單查詢"));
		new Actions(driver).click(menuList).perform();

		driver.switchTo().frame(1);
		//選擇資料查詢時段
		Thread.sleep(5000);
		WebElement datemin = driver.findElement(By.id("datemin"));
		String minDay = LocalDate.now().minusDays(period).toString();
		datemin.sendKeys(minDay);

		WebElement datemax = driver.findElement(By.id("datemax"));
		String maxDay = LocalDate.now().toString();
		datemax.sendKeys(maxDay);

		WebElement start_search_btn = driver.findElement(By.id("start_search_btn"));
		new Actions(driver).click(start_search_btn).perform();
		Thread.sleep(7000);

        //瀏覽器滑動至底部 找出翻頁用的超連結
		((JavascriptExecutor) driver).executeScript("window.scrollTo(0,document.body.scrollHeight)");
		Thread.sleep(3000);
		JSONArray requestArray = null;
		requestArray = new JSONArray();

		WebElement laypage = driver.findElement(By.cssSelector(".laypage_last"));
		String dataPage = laypage.getAttribute("data-page").toString();
		int dataPageI = Integer.parseInt(dataPage);
		System.out.println("共計"+dataPageI+"頁");
		int orderTableTotal;
		// 加一頁的目的
		// 在於最末頁無超連結 能可將剩餘資料撈出
		for (int j = 1; j <= dataPageI; j++) {
			try {
				System.out.println("第:" + j+ "頁");
				Thread.sleep(10000);
				orderTableTotal = getTableRow(driver, wait);
				System.out.println("共計:" + (orderTableTotal - 2) + "列資料");
				for (int i = 1; i <= orderTableTotal - 1; i++) {
					JSONObject obj = null;
					try {
						obj = new JSONObject();
						String rowI = String.valueOf(i);
						System.out.println("第:" + i + "列資料");

						WebElement getTable = driver
								.findElement(By.xpath("/html/body/form/div/div[3]/table/tbody/tr[" + rowI + "]/td[2]"));
						obj.put("序號", getTable.getText().toString());
						getTable = driver
								.findElement(By.xpath("/html/body/form/div/div[3]/table/tbody/tr[" + rowI + "]/td[3]"));
						obj.put("交易序號", getTable.getText().toString());
						getTable = driver
								.findElement(By.xpath("/html/body/form/div/div[3]/table/tbody/tr[" + rowI + "]/td[4]"));
						obj.put("服務代號", getTable.getText().toString());
						getTable = driver
								.findElement(By.xpath("/html/body/form/div/div[3]/table/tbody/tr[" + rowI + "]/td[5]"));
						obj.put("店號", getTable.getText().toString());
						getTable = driver
								.findElement(By.xpath("/html/body/form/div/div[3]/table/tbody/tr[" + rowI + "]/td[6]"));
						obj.put("機號", getTable.getText().toString());
						getTable = driver
								.findElement(By.xpath("/html/body/form/div/div[3]/table/tbody/tr[" + rowI + "]/td[7]"));
						obj.put("POS交易時間", getTable.getText());
						getTable = driver
								.findElement(By.xpath("/html/body/form/div/div[3]/table/tbody/tr[" + rowI + "]/td[8]"));
						obj.put("代收條碼", getTable.getText().toString());
						getTable = driver
								.findElement(By.xpath("/html/body/form/div/div[3]/table/tbody/tr[" + rowI + "]/td[9]"));
						obj.put("金額", getTable.getText().toString());
						getTable = driver.findElement(
								By.xpath("/html/body/form/div/div[3]/table/tbody/tr[" + rowI + "]/td[10]"));
						obj.put("建檔時間", getTable.getText().toString());
						requestArray.put(obj);
						System.out.println(obj.toString());
					} catch (NoSuchElementException e) {
						WebElement nextPage = driver.findElement(By.linkText("下一頁"));
						new Actions(driver).click(nextPage).perform();
						System.out.println(e.getRawMessage());
						Thread.sleep(5000);
					}
				}
			} catch (NoSuchElementException e) {
				System.out.println(e.getRawMessage());
				System.out.println("最末頁");
			}
		}
		System.out.println("共計:"+requestArray.length()+"筆資料");
		driver.close();
		JsonArrayToExcel exethis = new JsonArrayToExcel("okphone", "okphone");
		exethis.doExcelFromOkPhone(requestArray);
	}

	private static int getTableRow(WebDriver driver, WebDriverWait wait) {
		wait.until(ExpectedConditions.presenceOfElementLocated((By.xpath("/html/body/form/div/div[3]/table"))));
		WebElement tableDeal = driver.findElement(By.xpath("/html/body/form/div/div[3]/table"));
		List<WebElement> webList = tableDeal.findElements(By.tagName("tr"));
		return webList.size();
	}

}
