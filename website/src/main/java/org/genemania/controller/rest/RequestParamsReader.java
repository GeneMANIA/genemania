package org.genemania.controller.rest;

import java.lang.reflect.Method;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.genemania.controller.rest.GeneValidationController.ValidationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

public class RequestParamsReader<Params> {

	@Autowired
	private MappingJackson2HttpMessageConverter httpConverter;

	/**
	 * Reads the parameters into the specified parameters object
	 *
	 * @param req
	 *            The HTTP request containing either name=value&name2=value2
	 *            pairs or a JSON body
	 * @param params
	 *            The POJO into which the request params are placed (each setter
	 *            must have an alternative implementation that accepts a string
	 *            and converts it to the appropriate type)
	 */
	public void read(HttpServletRequest req, Params params) {
		String contentType = req.getHeader("Content-Type");
		contentType = contentType != null ? contentType : "";

		if (contentType.toLowerCase().contains("application/json")) {
			try {
				params = httpConverter.getObjectMapper()
						.readValue(req.getInputStream(),
								(Class<Params>) params.getClass());
			} catch (Exception e) {

			}
		} else { // assume form params

			// TODO use jackson to convert by constructing a json from the params

			Enumeration<String> paramNames = req.getParameterNames();
			while (paramNames.hasMoreElements()) {
				String paramName = paramNames.nextElement();
				String paramVal = req.getParameter(paramName);
				String fnName = "set" + paramName.toUpperCase().charAt(0)
						+ paramName.substring(1);

				try {
					Method method = params.getClass().getMethod(fnName,
							String.class);

					method.invoke(params, paramVal);
				} catch (Exception e) {
					// then skip this one
				}
			}

		}
	}

	public MappingJackson2HttpMessageConverter getHttpConverter() {
		return httpConverter;
	}

	public void setHttpConverter(
			MappingJackson2HttpMessageConverter httpConverter) {
		this.httpConverter = httpConverter;
	}

}
