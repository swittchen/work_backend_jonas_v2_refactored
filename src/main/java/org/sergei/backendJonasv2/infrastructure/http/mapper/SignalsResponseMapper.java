package org.sergei.backendJonasv2.infrastructure.http.mapper;

import org.sergei.backendJonasv2.domain.model.BioSource;
import org.sergei.backendJonasv2.infrastructure.http.PagedResponse;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

@Component
public class SignalsResponseMapper {

    public PagedResponse toPagedResponse(JsonNode json, int offset, int pageSize) {
        List<BioSource> items = new ArrayList<>();
        JsonNode data = json.path("data");
        if (data.isArray()) {
            for (JsonNode node : data) {
                items.add(toBioSource(node));
            }
        }
        boolean hasMore = items.size() == pageSize;
        return new PagedResponse(items, offset, hasMore);
    }

    public BioSource toBioSource(JsonNode node) {
        JsonNode attrs = node.path("attributes");
        return BioSource.builder()
                .eid(node.path("id").asText())
                .name(attrs.path("name").asText())
                .taxId(attrs.path("taxId").asText(null))
                .rank(attrs.path("rank").asText(null))
                .ancestorEid(attrs.path("ancestorEid").asText(null))
                .ancestorName(attrs.path("ancestorName").asText(null))
                .ancestorRank(attrs.path("ancestorRank").asText(null))
                .location(attrs.path("location").asText(null))
                .synonyms(List.of())
                .build();
    }

    public String toCreateRequestPayload(BioSource bioSource) {
        return """
                {
                  "data": {
                    "type": "assets",
                    "attributes": {
                      "name": "%s",
                      "taxId": "%s"
                    }
                  }
                }
                """.formatted(bioSource.getName(), bioSource.getTaxId());
    }
}
