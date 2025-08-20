package io.touchyongan.starter_template.common.data;

import lombok.Getter;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Getter
public class CustomPage<T> {
    private final PageData page;
    private final List<T> content;

    public CustomPage(final List<T> content,
                      final Pageable pageable,
                      final long total) {
        final var pageImpl = new PageImpl<>(content, pageable, total);
        this.content = pageImpl.getContent();
        this.page = new PageData(
                pageImpl.getSize(),
                pageImpl.getNumber(),
                pageImpl.getTotalElements(),
                pageImpl.getTotalPages());
    }
}
