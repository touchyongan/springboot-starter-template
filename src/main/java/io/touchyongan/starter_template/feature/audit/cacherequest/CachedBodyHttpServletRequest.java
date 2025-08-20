package io.touchyongan.starter_template.feature.audit.cacherequest;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.SneakyThrows;
import org.springframework.util.StreamUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
    private final byte[] cacheBody;

    @SneakyThrows
    public CachedBodyHttpServletRequest(final HttpServletRequest request) {
        super(request);
        if (isMultipart(request)) {
            cacheBody = null; // Don't cache multipart request
        } else {
            cacheBody = StreamUtils.copyToByteArray(request.getInputStream());
        }
    }

    private boolean isMultipart(final HttpServletRequest request) {
        return request.getContentType() != null && request.getContentType().startsWith("multipart/form-data");
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (Objects.nonNull(cacheBody)) {
            return new CacheBodyServletInputStream(cacheBody);
        }
        return super.getInputStream();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (Objects.nonNull(cacheBody)) {
            final var byteArrayInputStream = new ByteArrayInputStream(cacheBody);
            return new BufferedReader(new InputStreamReader(byteArrayInputStream, StandardCharsets.UTF_8));
        }
        return super.getReader();
    }
}
