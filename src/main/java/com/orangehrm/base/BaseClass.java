package com.orangehrm.base;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import com.orangehrm.actiondriver.ActionDriver;
import com.orangehrm.utilities.ExtentManager;
import com.orangehrm.utilities.LoggerManager;

public class BaseClass {

	protected static Properties prop;
	private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
	private static ThreadLocal<ActionDriver> actionDriver = new ThreadLocal<>();

	public static final Logger logger = LoggerManager.getLogger(BaseClass.class);

	@BeforeSuite
	public void loadConfig() throws IOException {
		prop = new Properties();
		FileInputStream fis = new FileInputStream("src/main/resources/config.properties");
		prop.load(fis);
		logger.info("Config.properties file loaded successfully");
	}

	@BeforeMethod
	public synchronized void setup() throws IOException {
		System.out.println("Setting up WebDriver for:" + this.getClass().getSimpleName());
		launchBrowser();
		configureBrowser();
		staticWait(2);
		logger.info("Webdriver initialized and browser maximized");

		// Bind the active driver instance to TestNG's execution context attributes
		if (getDriver() != null) {
			Reporter.getCurrentTestResult().setAttribute("WebDriverContext", getDriver());
		}

		actionDriver.set(new ActionDriver(getDriver()));
		logger.info("ActionDriver initialized for thread:" + Thread.currentThread().getId());
	}

	private synchronized void launchBrowser() {

		String browser = prop.getProperty("browser");

		if (browser.equalsIgnoreCase("chrome")) {

			// Create ChromeOptions instance to configure ChromeDriver

			ChromeOptions options = new ChromeOptions();
			// options.addArguments("--headless"); // Run Chrome in headless mode
			options.addArguments("--disable-gpu"); // Disable GPU for headless mode
			// options.addArguments("--window-size=1920,1080"); // Set window size
			options.addArguments("--disable-notifications"); // Disable browser notifications
			options.addArguments("--no-sandbox"); // Required for some CI environments like Jenkins
			options.addArguments("--disable-dev-shm-usage"); // Resolve issues in resource-limited environments

			driver.set(new ChromeDriver(options)); // New changes as per Thread
			ExtentManager.registerDriver(getDriver());
			logger.info("ChromeDriver Instance is created");
		} else if (browser.equalsIgnoreCase("firefox")) {

			// Create FirefoxOptions instance to configure FirefoxDriver
			FirefoxOptions options = new FirefoxOptions();
			options.addArguments("--headless"); // Run Firefox in headless mode
			options.addArguments("--disable-gpu"); // Disable GPU rendering (useful for headless mode)
			options.addArguments("--width=1920"); // Set browser width
			options.addArguments("--height=1080"); // Set browser height
			options.addArguments("--disable-notifications"); // Disable browser notifications
			options.addArguments("--no-sandbox"); // Needed for CI/CD environments
			options.addArguments("--disable-dev-shm-usage"); // Prevent crashes in low-resource environments

			driver.set(new FirefoxDriver(options)); // New changes as per Thread
			ExtentManager.registerDriver(getDriver());
			logger.info("FirefoxDriver Instance is created");
		} else if (browser.equalsIgnoreCase("edge")) {

			EdgeOptions options = new EdgeOptions();
			options.addArguments("--headless"); // Run Edge in headless mode
			options.addArguments("--disable-gpu"); // Disable GPU acceleration
			options.addArguments("--window-size=1920,1080"); // Set window size
			options.addArguments("--disable-notifications"); // Disable pop-up notifications
			options.addArguments("--no-sandbox"); // Needed for CI/CD
			options.addArguments("--disable-dev-shm-usage"); // Prevent resource-limited crashes

			driver.set(new EdgeDriver(options)); // New changes as per Thread
			ExtentManager.registerDriver(getDriver());
			logger.info("EdgeDriver Instance is created");
		} else {
			throw new IllegalArgumentException("Browser not supported:" + browser);
		}
	}

	private void configureBrowser() {
		int implicitWait = Integer.parseInt(prop.getProperty("implicitWait"));
		getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
		getDriver().manage().window().maximize();
		try {
			getDriver().get(prop.getProperty("url"));
		} catch (Exception e) {
			System.out.println("Failed to navigate URL:" + e.getMessage());
		}
	}

	@AfterMethod
	public synchronized void teardown() {
		if (getDriver() != null) {
			try {
				getDriver().quit();
			} catch (Exception e) {
				logger.info("unable to quit driver:" + e.getMessage());
			}
		}
		logger.info("Webdriver instance is closed");
		driver.remove();
		actionDriver.remove();
	}

	public static WebDriver getDriver() {
		if (driver.get() == null) {
			System.out.println("WebDriver is not initialized.");
			throw new IllegalStateException("WebDriver is not initialized.");
		}
		return driver.get();
	}

	public static ActionDriver getActionDriver() {
		if (actionDriver.get() == null) {
			System.out.println("ActionDriver is not initialized.");
			throw new IllegalStateException("ActionDriver is not initialized.");
		}
		return actionDriver.get();
	}

	public static Properties getProp() {
		return prop;
	}

	public void setDriver(ThreadLocal<WebDriver> driver) {
		this.driver = driver;
	}

	public void staticWait(int seconds) {
		LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(seconds));
	}
}