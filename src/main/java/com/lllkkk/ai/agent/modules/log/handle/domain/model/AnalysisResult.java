package com.lllkkk.ai.agent.modules.log.handle.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisResult {
    public String rootCause;
    public String summary;
    public String fixSuggestion;
    public String relatedLocation;
}
