package com.orangehrm.listeners;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.IAnnotationTransformer;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.ITestAnnotation;

import com.orangehrm.utilities.ExtentManager;
import com.orangehrm.utilities.RetryAnalyzer;

public class TestListener implements ITestListener, IAnnotationTransformer {

	@Override
	public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {

		annotation.setRetryAnalyzer(RetryAnalyzer.class);
	}

	@Override
	public void onTestStart(ITestResult result) {
		String testName = result.getMethod().getMethodName();
		ExtentManager.startTest(testName);
		ExtentManager.logStep("Test Started: " + testName);
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		String testName = result.getMethod().getMethodName();
		WebDriver driver = getDriverFromContext(result);

		if (driver != null && isSessionActive(driver)) {
			ExtentManager.logStepWithScreenshot(driver, "Test Passed Successfully!",
					"Test End: " + testName + "- ✔ Test Passed");
		} else {
			ExtentManager
					.logStep("Test End: " + testName + "- ✔ Test Passed (Screenshot skipped: Driver session closed)");
		}
	}

	// Triggered when a test fails
	@Override
	public void onTestFailure(ITestResult result) {
		String testName = result.getMethod().getMethodName();
		String failureMessage = result.getThrowable() != null ? result.getThrowable().getMessage()
				: "No error message provided";
		ExtentManager.logStep(failureMessage);

		WebDriver driver = getDriverFromContext(result);

		if (driver != null && isSessionActive(driver)) {
			ExtentManager.logFailure(driver, "Test Failed!", "Test End: " + testName + "- ❌ Test Failed");
		} else {
			ExtentManager
					.logStep("Test End: " + testName + "- ❌ Test Failed (Screenshot skipped: Driver session closed)");
		}
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		String testName = result.getMethod().getMethodName();
		ExtentManager.logSkip("Test Skipped!" + testName + "- ⚠ Test Skipped");
	}

	@Override
	public void onStart(ITestContext context) {
		ExtentManager.getReporter();
	}

	@Override
	public void onFinish(ITestContext context) {
		ExtentManager.endTest();
	}

	// Helper to extract the driver stored inside the TestNG result bank safely
	private WebDriver getDriverFromContext(ITestResult result) {
		Object driverObject = result.getAttribute("WebDriverContext");
		if (driverObject instanceof WebDriver) {
			return (WebDriver) driverObject;
		}
		return null;
	}

	// Safety check to avoid taking screenshots on already terminated sessions
	private boolean isSessionActive(WebDriver driver) {
		try {
			if (driver instanceof RemoteWebDriver) {
				return ((RemoteWebDriver) driver).getSessionId() != null;
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}