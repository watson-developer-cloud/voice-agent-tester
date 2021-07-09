/*
 * (C) Copyright IBM Corp. 2019, 2021.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
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
