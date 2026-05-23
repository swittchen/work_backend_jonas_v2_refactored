package org.sergei.backendJonasv2.infrastructure.http;

import org.sergei.backendJonasv2.domain.model.BioSource;

import java.util.List;

public record PagedResponse(List<BioSource> items, int offset, boolean hasMore) {}
