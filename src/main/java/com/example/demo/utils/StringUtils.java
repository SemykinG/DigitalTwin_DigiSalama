package com.example.demo.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringUtils {

	// HAS LIMITED PRIVILEGES
	public final static String ROLE_USER = "ROLE_USER";

	// CAN MANAGE ALL DATA INSIDE OWN ORGANISATION
	public final static String ROLE_ORGANISATION_ADMIN = "ROLE_ORGANISATION_ADMIN";

	// HAS ALL PRIVILEGES
	public final static String ROLE_SYSTEM_ADMIN = "ROLE_SYSTEM_ADMIN";

	public final static String FORWARD_SLASH = "/";
	public final static String UI_API = "/api1";
	public final static String JSON_API = "/api2";
	public final static String ID = "/{id}";
	public final static String NEW = "/new";
	public final static String SAVE = "/save";
	public final static String EDIT = "/edit";
	public final static String UPDATE = "/update";
	public final static String MODIFY = "/modify";
	public final static String DELETE = "/delete";
	public final static String REDIRECT_URL = "redirect:";

	public final static String CHANGES_ALLOWED_ATTRIBUTE = "changes_allowed";

	public final static String HOME_PAGE =            "home_page";
	public final static String ERROR_PAGE =               "error1";
	public final static String ERROR_TITLE_ATTRIBUTE =    "error_title_attribute";
	public final static String ERROR_MESSAGE_ATTRIBUTE =  "error_message_attribute";
	public final static String EXCEPTION_ATTRIBUTE =  "exception_attribute";


	public static byte[] stringToBytes(String str) {
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		assert messageDigest != null;
		return messageDigest.digest(str.getBytes(StandardCharsets.UTF_8));
	}

	private static String bytesToHex(byte[] hash) {
		StringBuilder hexString = new StringBuilder(2 * hash.length);

		for (int i = 0; i < hash.length; i++) {
			String hex = Integer.toHexString(0xff & hash[i]);
			if(hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}

	public static String generateHashFromString(String string) {
		return bytesToHex(stringToBytes(string));
	}

}