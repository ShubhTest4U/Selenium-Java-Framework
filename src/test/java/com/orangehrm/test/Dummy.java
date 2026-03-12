package com.orangehrm.test;

import org.testng.annotations.Test;

import com.orangehrm.base.BaseClass;

public class Dummy extends BaseClass{
	
	@Test
	public void dummyTest() {
		
		String title = driver.getTitle();
		assert title.equals("OrangeHRM"):"Test faild - Title not matching";
		
		System.out.println("Test passed - Title is matching");
	}

}

