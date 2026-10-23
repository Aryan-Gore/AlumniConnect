package com.alumniconnect.backend.dto.group;

import com.alumniconnect.backend.dto.common.InstituteRef;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class InstituteGroups {
    private InstituteRef institute;
    private List<GroupSummary> groups;
}
