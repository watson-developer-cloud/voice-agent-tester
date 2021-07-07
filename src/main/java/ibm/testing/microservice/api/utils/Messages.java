/* ***************************************************************** */
/*                                                                   */
/* Licensed Materials - Property of IBM                              */
/*                                                                   */
/* (C) Copyright IBM Corp. 2019. All Rights Reserved.                */
/*                                                                   */
/* US Government Users Restricted Rights - Use, duplication or       */
/* disclosure restricted by GSA ADP Schedule Contract with IBM Corp. */
/*                                                                   */
/* ***************************************************************** */
package ibm.testing.microservice.api.utils;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {

	private static final String BUNDLE_NAME = "cwsat";

	public static String getMessage(String resourceId, Locale locale, Object[] params) {
		String msg = null;
		try {
          ResourceBundle rb = ResourceBundle.getBundle(BUNDLE_NAME);
          msg = rb.getString(resourceId);
          if (params != null && params.length != 0) {
              msg = MessageFormat.format(msg, params);
          }
      } catch (MissingResourceException e) {
          msg = '!' + " {" + locale.getLanguage() + "} " + resourceId + '!';
      }		
		return msg;
	}

	public static String getMessage(String resourceId) {
		return getMessage(resourceId, Locale.getDefault(), null);
	}
	
	public static String getMessage(String resourceId, Object[] params) {
      return getMessage(resourceId, Locale.getDefault(), params);
  }
	
	public static String getMessage(String resourceId, String param) {
      return getMessage(resourceId,new String[] {param});
  }
	
	
}
