package ru.lessons.my.filter;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@WebFilter(urlPatterns = "/api/v1/*")
public class StatsFilter implements Filter {

    private LongHistogram requestDuration;

    public void init(FilterConfig filterConfig) {
        Meter meter = GlobalOpenTelemetry.getMeter("vehiclePark");
        requestDuration = meter
                .histogramBuilder("my.custom.duration.metric")
                .setDescription("Duration of HTTP requests in milliseconds")
                .setUnit("ms")
                .ofLongs()
                .build();
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        long time = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            time = System.currentTimeMillis() - time;
            if (request instanceof HttpServletRequest req) {
                log.info("Execution total time of {}, Request: {} = {} ms", req.getMethod(), req.getRequestURI(), time);
                Attributes attributes = Attributes.builder()
                        .put("http.method", req.getMethod())
                        .put("http.route", req.getRequestURI())
                        .build();
                requestDuration.record(time, attributes);
            }
        }
    }
}
