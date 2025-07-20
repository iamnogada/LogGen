package net.kubepia.loggen.controller;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Map;

@RestControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Map<String, Object>> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return Map.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Map<String, Object> beforeBodyWrite(Map<String, Object> body, MethodParameter returnType,
                                              MediaType selectedContentType,
                                              Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                              ServerHttpRequest request, ServerHttpResponse response) {
        
        if (body != null && body.containsKey("_httpStatus")) {
            Integer statusCode = (Integer) body.remove("_httpStatus");
            response.setStatusCode(HttpStatus.valueOf(statusCode));
        }
        
        return body;
    }
} 