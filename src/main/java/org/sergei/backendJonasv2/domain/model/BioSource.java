package org.sergei.backendJonasv2.domain.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.util.List;

/**
 * Reines Domain_Modell - KEIN @Entity, KEIN @Component
 * Unabhängig von Spring, JPA und HTTP.
 * Kann in jedem Layer benutzt werden.
 */

@Value
@Builder
@With
public class BioSource {
    String eid;
    String name;
    String taxId;
    String rank;
    List<String> synonyms;
    String ancestorEid;
    String ancestorName;
    String ancestorRank;
    String location;
}
