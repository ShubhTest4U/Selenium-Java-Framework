package com.orangehrm.base;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

//import net.bytebuddy.implementation.bind.annotation.DefaultCall.Binder.DefaultMethodLocator.Implicit;

public class BaseClass {

	protected static Properties prop;
	protected WebDriver driver;

	@BeforeSuite
	// Load the configuration file
	public void loadConfig() throws IOException {

		prop = new Properties();
		FileInputStream fis = new FileInputStream("src/resources/config.properties");
		prop.load(fis);
	}

	// Initialize the webdriver based on browser defined in config.properties file
	private void launchBrowser() {

		String browser = prop.getProperty("browser");

		if (browser.equalsIgnoreCase("chrome")) {
			driver = new ChromeDriver();
		} else if (browser.equalsIgnoreCase("firefox")) {
			driver = new FirefoxDriver();
		} else if (browser.equalsIgnoreCase("edge")) {
			driver = new EdgeDriver();
		} else {
			throw new IllegalArgumentException("Browser not supported:" + browser);

		}

	}

	
//Configure browser setting such as implicit wait, maximize browser, navigate to URL	
	private void configureBrowser() {
		// Implicit wait
		int implicitWait = Integer.parseInt(prop.getProperty("implicitWait"));
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));

		// Maximize the browser
		driver.manage().window().maximize();

		// Navigate to URL
		try {
			driver.get(prop.getProperty("url"));
		} catch (Exception e) {
			System.out.println("Failed to navigate URL:"+e.getMessage());
		}
	}

	@BeforeMethod
	public void setup() throws IOException 
	{
		System.out.println("Setting up WebDriver for:"+this.getClass().getSimpleName());
		launchBrowser();
		configureBrowser();
		staticWait(2);
		

	}

	@AfterMethod
	public void teardown() {
		if (driver != null) {
			try {
				driver.quit();
			} catch (Exception e) {
				System.out.println("unable to quit driver:"+e.getMessage());
			}
		}
	
	}
	
	//Getter method for prop
	public static Properties getProp()
	{
		return prop;
	}
	
	//Driver getter method
	public WebDriver getDriver() 
	{
		return driver;
	}
	
	//Driver setter method
		public WebDriver setDriver() 
		{
			return driver;
		}
	
	//Static wait for pause
	public void staticWait(int seconds) {
		LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(seconds));
	}

}
