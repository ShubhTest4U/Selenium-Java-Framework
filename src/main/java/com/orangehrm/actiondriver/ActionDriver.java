package com.orangehrm.actiondriver;

import java.time.Duration;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.orangehrm.base.BaseClass;

import net.bytebuddy.asm.Advice.Return;

public class ActionDriver {
	
	private WebDriver driver;
	private WebDriverWait wait;
	public static final Logger logger = BaseClass.logger;
	
	public ActionDriver(WebDriver driver) 
	{
		this.driver = driver;
		int explicitWait = Integer.parseInt(BaseClass.getProp().getProperty("explicitWait"));
		this.wait = new WebDriverWait(driver, Duration.ofSeconds(explicitWait));
		logger.info("WebDriver instance is created.");
	}
	
	//Method to click element
	public void click(By by) 
	{
		try {
			driver.findElement(by).click();
			logger.info("Clicked an element");
		} catch (Exception e) {
			System.out.println("Unable to click element: "+e.getMessage());
			logger.error("Unable to click element");
		}
		
	}
	
	
	//Method to enter text into an input field
	public void enterText(By by, String value)
	{
		try {
			waitForElementToBeVisible(by);
			WebElement element = driver.findElement(by);
			element.clear();
			element.sendKeys(value);
			logger.info("Entered text: "+value);
		} catch (Exception e) {
			logger.error("Unable to enter the value: "+e.getMessage());
		}	
	}
	
	//Method to get text from an input field
	public String getText(By by) 
	{
		try {
			waitForElementToBeVisible(by);
			return driver.findElement(by).getText();
		} catch (Exception e) {
			logger.error("unable to get the text: "+e.getMessage());
			return "";
		}
	}
	
	//Method to compare Two text
	public boolean compareText(By by, String expectedText) 
	{
		try {
			waitForElementToBeVisible(by);
			String actualText = driver.findElement(by).getText();
			if(expectedText.equals(actualText))
			{
				logger.info("Texts are matching: "+actualText+ " equals with text: "+expectedText);
				return true;
			}
			else {
				logger.error("Texts are not matching: "+actualText+" not equals with text: "+expectedText);
				return false;
			}
		} catch (Exception e) {
			logger.error("Unable to compare Texts: "+e.getMessage());
		}
		return false;
	}
	
	//Method to check if an element is displayed
	public boolean isDisplayed(By by) 
	{
		try {
			waitForElementToBeVisible(by);
			return driver.findElement(by).isDisplayed();
		} catch (Exception e) {
			logger.error("Element is not displayed: "+e.getMessage());
			return false;
		}
		
	}
	
	// Wait for the page to load
		public void waitForPageLoad(int timeOutInSec) {
			try {
				wait.withTimeout(Duration.ofSeconds(timeOutInSec)).until(WebDriver -> ((JavascriptExecutor) WebDriver)
						.executeScript("return document.readyState").equals("complete"));
				logger.info("Page loaded successfully.");
			} catch (Exception e) {
				logger.error("Page did not load within " + timeOutInSec + " seconds. Exception: " + e.getMessage());
			}
		}

	
	//Scroll to an element
	public void scrollToElement(By by) 
	{
		try {
			JavascriptExecutor js = (JavascriptExecutor) driver;
			WebElement element = driver.findElement(by);
			js.executeScript("arguments[0],scrollIntoView(true);", element);
		} catch (Exception e) {
			logger.error("Unable to locate element: "+e.getMessage());
		}
		
	}
	
	//Wait for element to be clickable
	private void waitForElementToBeClickable(By by) 
	{
		try {
			wait.until(ExpectedConditions.elementToBeClickable(by));
		} catch (Exception e) {
			logger.error("Element is not clickable: "+e.getMessage());
		}
	}
	
	//Wait for element to be visible
	private void waitForElementToBeVisible(By by)
	{
		wait.until(ExpectedConditions.visibilityOfElementLocated(by));
	}

}
