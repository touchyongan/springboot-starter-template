package io.touchyongan.starter_template.feature.audit.cacherequest;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CacheBodyServletInputStream extends ServletInputStream {
    private final InputStream cacheInputStream;

    public CacheBodyServletInputStream(final byte[] cacheBody) {
        this.cacheInputStream = new ByteArrayInputStream(cacheBody);
    }

    @SneakyThrows
    @Override
    public boolean isFinished() {
        return cacheInputStream.available() == 0;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(final ReadListener readListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int read() throws IOException {
        return cacheInputStream.read();
    }
}
