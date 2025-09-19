package ru.lessons.my.filter;


import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import ru.lessons.my.util.TimeZoneContext;

import java.io.IOException;

public class TimeZoneFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {
        TimeZoneContext.set(request.getParameter("timeZone"));

        chain.doFilter(request, response);

        TimeZoneContext.reset();
    }
}
