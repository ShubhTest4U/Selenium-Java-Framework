package com.orangehrm.test;

import org.testng.annotations.Test;

import com.orangehrm.base.BaseClass;
import com.orangehrm.utilities.ExtentManager;

public class Dummy2 extends BaseClass {

	@Test
	public void dummyTest() {

		ExtentManager.startTest("DummyTest2 Test");
		String title = getDriver().getTitle();
		ExtentManager.logStep("verifying the title");
		assert title.equals("OrangeHRM") : "Test Failed - Title is Not Matching";

		System.out.println("Test Passed - Title is Matching");
		ExtentManager.logStep("Validation Successful");
	}

}
