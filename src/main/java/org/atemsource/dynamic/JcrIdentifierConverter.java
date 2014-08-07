package org.atemsource.dynamic;

import org.atemsource.atem.utility.transform.api.JavaConverter;
import org.atemsource.atem.utility.transform.api.TransformationContext;

public class JcrIdentifierConverter implements JavaConverter<String,String> {

	@Override
	public String convertAB(String a, TransformationContext ctx) {
		return a.replace("/", ":");
	}

	@Override
	public String convertBA(String b, TransformationContext ctx) {
		return b.replace(":", "/");
	}

}
