package com.orangehrm.test;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.orangehrm.base.BaseClass;
import com.orangehrm.pages.HomePage;
import com.orangehrm.pages.LoginPage;

public class LoginPageTest extends BaseClass{
	
	private LoginPage loginPage;
	private HomePage homePage;
	
	@BeforeMethod
	public void setupPages() 
	{
		loginPage = new LoginPage(getDriver());
		homePage = new HomePage(getDriver());		
	}
	
	@Test
	public void verifyValidLoginTest() 
	{
		loginPage.login("orangehrm_shubh", "Shubh@1234");
		Assert.assertTrue(homePage.isAdminTabVisible(),"Admin tab should be visible after successfull login");
		homePage.logout();
		staticWait(2);
	}
	@Test
	public void invalidLoginTest() 
	{
		loginPage.login("orangehrm_shubh", "Shubh@124");
		String expectedErrorMessage = "Invalid credential6";
		Assert.assertTrue(loginPage.verifyErrorMessage(expectedErrorMessage), "Test failed: Invalid Error message");
	}
}
