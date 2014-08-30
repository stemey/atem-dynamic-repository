package org.atemsource.dynamic;

import org.atemsource.atem.utility.transform.api.JavaConverter;
import org.atemsource.atem.utility.transform.api.JavaUniConverter;
import org.atemsource.atem.utility.transform.api.TransformationContext;

public class JcrTypeCodeConverter implements JavaConverter<String, String>{

	public static final String JCR_PREFIX = "jcr:";
	public static final String JSON_PREFIX = "";



	@Override
	public String convertBA(String a, TransformationContext ctx) {
		return JCR_PREFIX+a.substring(JSON_PREFIX.length());
	}

	

	@Override
	public String convertAB(String b, TransformationContext ctx) {
		return JSON_PREFIX+b.substring(JCR_PREFIX.length());
	}

}
