package com.orangehrm.base;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterMethod;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import com.orangehrm.actiondriver.ActionDriver;
import com.orangehrm.utilities.ExtentManager;
import com.orangehrm.utilities.LoggerManager;

//import net.bytebuddy.implementation.bind.annotation.DefaultCall.Binder.DefaultMethodLocator.Implicit;

public class BaseClass {

	protected static Properties prop;
	// protected static WebDriver driver;
	// protected static ActionDriver actionDriver;

	private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
	private static ThreadLocal<ActionDriver> actionDriver = new ThreadLocal<>();

	public static final Logger logger = LoggerManager.getLogger(BaseClass.class);

	@BeforeSuite
	// Load the configuration file
	public void loadConfig() throws IOException {

		prop = new Properties();
		FileInputStream fis = new FileInputStream("src/main/resources/config.properties");
		prop.load(fis);
		logger.info("Config.properties file loaded successfully");

		// Start the ExtentReport
		//ExtentManager.getReporter(); -- This has been implemented in test listener class
	}

	@BeforeMethod
	public synchronized void setup() throws IOException {
		System.out.println("Setting up WebDriver for:" + this.getClass().getSimpleName());
		launchBrowser();
		configureBrowser();
		staticWait(2);
		logger.info("Webdriver initialized and browser maximized");
		logger.trace("This is a trace log message");
		logger.error("This is an error log message");
		logger.warn("This is a warning log message");
		logger.debug("This is a debug log message");
		logger.fatal("This is a fatal log message");

		/*
		 * //Initialize ActionDriver only once
		 * 
		 * if(actionDriver == null) { actionDriver = new ActionDriver(driver);
		 * logger.info("ActionDriver instance created for:"+Thread.currentThread().getId
		 * ()); }
		 */

		// Initialize ActionDriver for current thread
		actionDriver.set(new ActionDriver(getDriver()));
		logger.info("ActionDriver initialized for thread:" + Thread.currentThread().getId());

	}

// Initialize the webdriver based on browser defined in config.properties file

	private synchronized void launchBrowser() {

		String browser = prop.getProperty("browser");

		if (browser.equalsIgnoreCase("chrome")) {
			// driver = new ChromeDriver();
			driver.set(new ChromeDriver()); // New changes as per ThreadLocal implementation
			ExtentManager.registerDriver(getDriver());
			logger.info("ChromeDriver Instance is created");
		} else if (browser.equalsIgnoreCase("firefox")) {
			// driver = new FirefoxDriver();
			driver.set(new FirefoxDriver()); // New changes as per ThreadLocal implementation
			ExtentManager.registerDriver(getDriver());
			logger.info("FirefoxDriver Instance is created");
		} else if (browser.equalsIgnoreCase("edge")) {
			// driver = new EdgeDriver();
			driver.set(new EdgeDriver()); // New changes as per ThreadLocal implementation
			ExtentManager.registerDriver(getDriver());
			logger.info("EdgeDriver Instance is created");
		} else {
			throw new IllegalArgumentException("Browser not supported:" + browser);

		}

	}

//Configure browser setting such as implicit wait, maximize browser, navigate to URL	

	private void configureBrowser() {
		// Implicit wait
		int implicitWait = Integer.parseInt(prop.getProperty("implicitWait"));
		getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));

		// Maximize the browser
		getDriver().manage().window().maximize();

		// Navigate to URL
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
//		driver=null;
//		actionDriver=null;
		//ExtentManager.endTest(); -- This has been implemented in test listener class
	}

	/*
	 * 
	 * 
	 * //Driver getter method public WebDriver getDriver() { return driver; }
	 */
	// Getter method for WebDriver
	public static WebDriver getDriver() {
		if (driver.get() == null) {
			System.out.println("WebDriver is not initialized.");
			throw new IllegalStateException("WebDriver is not initialized.");
		}
		return driver.get();
	}

	// Getter method for ActionDriver
	public static ActionDriver getActionDriver() {
		if (actionDriver.get() == null) {
			System.out.println("ActionDriver is not initialized.");
			throw new IllegalStateException("ActionDriver is not initialized.");
		}
		return actionDriver.get();
	}

	// Getter method for prop
	public static Properties getProp() {
		return prop;
	}

	// Driver setter method
	public void setDriver(ThreadLocal<WebDriver> driver) {
		this.driver = driver;
	}

	// Static wait for pause
	public void staticWait(int seconds) {
		LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(seconds));
	}

}
