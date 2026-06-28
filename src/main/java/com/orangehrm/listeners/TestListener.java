package com.orangehrm.listeners;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.orangehrm.base.BaseClass;
import com.orangehrm.utilities.ExtentManager;

public class TestListener implements ITestListener {

	// Triggered when a test starts
	@Override
	public void onTestStart(ITestResult result) {
		String testName = result.getMethod().getMethodName();
		// Start logging in ExtentReport
		ExtentManager.startTest(testName);
		ExtentManager.logStep("Test Started: " + testName);

	}

	// Triggered when a test succeeds
	@Override
	public void onTestSuccess(ITestResult result) {
		String testName = result.getMethod().getMethodName();
		ExtentManager.logStepWithScreenshot(BaseClass.getDriver(), "Test Passed Successfully!",
				"Test End: " + testName + "- ✔ Test Passed");

	}

	// Triggered when a test fails
	@Override
	public void onTestFailure(ITestResult result) {

		String testName = result.getMethod().getMethodName();
		String failureMessage = result.getThrowable().getMessage();
		ExtentManager.logStep(failureMessage);
		ExtentManager.logFailure(BaseClass.getDriver(), "Test Failed!", "Test End: " + testName + "- ❌ Test Failed");

	}

	// triggered when a test is skipped
	@Override
	public void onTestSkipped(ITestResult result) {

		String testName = result.getMethod().getMethodName();
		ExtentManager.logSkip("Test Skipped!" + testName + "- ⚠ Test Skipped");
	}

	// Triggered when a suite starts
	@Override
	public void onStart(ITestContext context) {
		// Initialize the ExtentReport
		ExtentManager.getReporter();

	}

	// Triggered when a suite ends
	@Override
	public void onFinish(ITestContext context) {
		// Flush the ExtentReport
		ExtentManager.endTest();
	}

}
