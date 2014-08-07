package org.atemsource.dynamic;

import org.atemsource.atem.utility.transform.api.JavaConverter;
import org.atemsource.atem.utility.transform.api.JavaUniConverter;
import org.atemsource.atem.utility.transform.api.TransformationContext;

public class JcrTypeCodeConverter implements JavaConverter<String, String>{

	private static final String JCR_PREFIX = "jcr:";



	@Override
	public String convertBA(String a, TransformationContext ctx) {
		return JCR_PREFIX+a;
	}

	

	@Override
	public String convertAB(String b, TransformationContext ctx) {
		return b.substring(JCR_PREFIX.length());
	}

}
