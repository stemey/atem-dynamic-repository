package org.atemsource.dynamic;

import org.atemsource.atem.utility.transform.api.JavaUniConverter;
import org.atemsource.atem.utility.transform.api.TransformationContext;

public class JcrAttributeCodeConverter implements
		JavaUniConverter<String, String> {

	@Override
	public String convert(String a, TransformationContext ctx) {
		if (a.equals("url")) {
			return "path";
		}  else {
			return a;
		}
	}

}
