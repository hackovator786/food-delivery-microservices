package com.hackovation.userservice.exception;

public class ApiException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public ApiException(String msg) {
		super(msg);
	}

}