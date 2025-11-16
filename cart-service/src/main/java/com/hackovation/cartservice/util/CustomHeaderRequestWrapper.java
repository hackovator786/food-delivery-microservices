package com.hackovation.cartservice.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.*;

public class CustomHeaderRequestWrapper extends HttpServletRequestWrapper {
    private final Map<String, String> customHeaders;

    public CustomHeaderRequestWrapper(HttpServletRequest request, Map<String, String> customHeaders) {
        super(request);
        this.customHeaders = new HashMap<>(customHeaders);
    }

    @Override
    public String getHeader(String name) {
        String headerValue = customHeaders.get(name);
        if (headerValue != null) {
            return headerValue;
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> names = new HashSet<>(customHeaders.keySet());
        Enumeration<String> originalNames = super.getHeaderNames();
        while (originalNames.hasMoreElements()) {
            names.add(originalNames.nextElement());
        }
        return Collections.enumeration(names);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if (customHeaders.containsKey(name)) {
            return Collections.enumeration(List.of(customHeaders.get(name)));
        }
        return super.getHeaders(name);
    }
}
