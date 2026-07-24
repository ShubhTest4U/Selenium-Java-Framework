package com.orangehrm.base;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;


import org.testng.annotations.Parameters;
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
	@Parameters("browser")
	public synchronized void setup(String browser) throws IOException {
		System.out.println("Setting up WebDriver for:" + this.getClass().getSimpleName());
		launchBrowser(browser);
		configureBrowser();
		staticWait(2);	
		// Sample logger message
		logger.info("WebDriver Initialized and Browser Maximized");
		logger.trace("This is a Trace message");
		logger.error("This is a error message");
		logger.debug("This is a debug message");
		logger.fatal("This is a fatal message");
		logger.warn("This is a warm message");

		// Bind the active driver instance to TestNG's execution context attributes
		if (getDriver() != null) {
			Reporter.getCurrentTestResult().setAttribute("WebDriverContext", getDriver());
		}

		// Initialize ActionDriver for the current thread
		actionDriver.set(new ActionDriver(getDriver()));
		logger.info("ActionDriver initialized for thread:" + Thread.currentThread().getId());
	}

	private synchronized void launchBrowser(String browser) {

	    boolean seleniumGrid = Boolean.parseBoolean(prop.getProperty("seleniumGrid"));
	    String gridURL = prop.getProperty("gridURL");
	    boolean isHeadless = Boolean.parseBoolean(prop.getProperty("headless"));
	    
	    if (seleniumGrid) {
	        try {
	            if (browser.equalsIgnoreCase("chrome")) {
	                ChromeOptions options = new ChromeOptions();
	                if (isHeadless) {
	                    options.addArguments("--headless=new", "--disable-gpu");
	                }
	                options.addArguments("--window-size=1920,1080");
	                driver.set(new RemoteWebDriver(new URL(gridURL), options));
	                
	            } else if (browser.equalsIgnoreCase("firefox")) {
	                FirefoxOptions options = new FirefoxOptions();
	                if (isHeadless) {
	                    options.addArguments("-headless");
	                }
	                driver.set(new RemoteWebDriver(new URL(gridURL), options));
	                
	            } else if (browser.equalsIgnoreCase("edge")) {
	                EdgeOptions options = new EdgeOptions();
	                if (isHeadless) {
	                    options.addArguments("--headless=new", "--disable-gpu");
	                }
	                options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
	                driver.set(new RemoteWebDriver(new URL(gridURL), options));
	                
	            } else {
	                throw new IllegalArgumentException("Browser Not Supported: " + browser);
	            }
	            
	            // FIX: Register driver for Extent Reports during Grid execution
	            ExtentManager.registerDriver(getDriver());
	            logger.info("RemoteWebDriver instance created for Grid (Headless: " + isHeadless + ")");
	            
	        } catch (MalformedURLException e) {
	            throw new RuntimeException("Invalid Grid URL", e);
	        }
	    } else {
	        // Local execution logic...
	        if (browser.equalsIgnoreCase("chrome")) {
	            ChromeOptions options = new ChromeOptions();
	            if (isHeadless) {
	                options.addArguments("--headless=new", "--disable-gpu");
	            }
	            options.addArguments("--disable-notifications", "--no-sandbox", "--disable-dev-shm-usage");
	            driver.set(new ChromeDriver(options));
	            ExtentManager.registerDriver(getDriver());
	            logger.info("ChromeDriver Instance is created.");
	            
	        } else if (browser.equalsIgnoreCase("firefox")) {
	            FirefoxOptions options = new FirefoxOptions();
	            if (isHeadless) {
	                options.addArguments("-headless");
	            }
	            options.addArguments("--width=1920", "--height=1080", "--disable-notifications", "--no-sandbox", "--disable-dev-shm-usage");
	            driver.set(new FirefoxDriver(options));
	            ExtentManager.registerDriver(getDriver());
	            logger.info("FirefoxDriver Instance is created.");
	            
	        } else if (browser.equalsIgnoreCase("edge")) {
	            EdgeOptions options = new EdgeOptions();
	            if (isHeadless) {
	                options.addArguments("--headless=new", "--disable-gpu");
	            }
	            options.addArguments("--window-size=1920,1080", "--disable-notifications", "--no-sandbox", "--disable-dev-shm-usage");
	            driver.set(new EdgeDriver(options));
	            ExtentManager.registerDriver(getDriver());
	            logger.info("EdgeDriver Instance is created.");
	        } else {
	            throw new IllegalArgumentException("Browser Not Supported:" + browser);
	        }
	    }
	    
	    // FIX: Accurately determine if execution is Grid vs Local
	    if (getDriver().getClass().equals(RemoteWebDriver.class)) {
			RemoteWebDriver remoteDriver = (RemoteWebDriver) getDriver();
			logger.info("--> Executing via SELENIUM GRID");
			logger.info("--> Remote Session ID: " + remoteDriver.getSessionId());
		} else {
			logger.info("--> Executing via LOCAL Web Driver");
		}
	}

	private void configureBrowser() {
		// FIX: Check System properties first, fallback to config.properties, and trim spaces
		String waitStr = System.getProperty("implicitWait", prop.getProperty("implicitWait"));
		int implicitWait = Integer.parseInt(waitStr.trim());
		
		boolean seleniumGrid = Boolean.parseBoolean(System.getProperty("seleniumGrid", prop.getProperty("seleniumGrid")));
		
		getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
		getDriver().manage().window().maximize();
		
		if (seleniumGrid) {
			getDriver().get(prop.getProperty("url_grid"));
		} else {
			getDriver().get(prop.getProperty("url_base"));
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