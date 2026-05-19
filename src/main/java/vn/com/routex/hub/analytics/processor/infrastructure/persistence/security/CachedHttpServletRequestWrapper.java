package vn.com.routex.hub.analytics.processor.infrastructure.persistence.security;

import jakarta.annotation.PreDestroy;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.web.context.annotation.RequestScope;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@RequestScope
public class CachedHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] cachedRequest;

    public CachedHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        this.cachedRequest = request.getInputStream().readAllBytes();
    }

    @Override
    public ServletInputStream getInputStream() {
        return new CachedServletInputStream(this.cachedRequest);
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(this.cachedRequest), StandardCharsets.UTF_8));
    }

    private static class CachedServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream byteArrayInputStream;

        public CachedServletInputStream(byte[] cachedRequest) {
            this.byteArrayInputStream = new ByteArrayInputStream(cachedRequest);
        }

        @Override
        public boolean isFinished() {
            return byteArrayInputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
        }

        @Override
        public int read() throws IOException {
            return byteArrayInputStream.read();
        }
    }

    @PreDestroy
    public void traceDestroy() {
        System.out.println("CustomerHttpServletRequestWrapper be destroyed");
    }
}
