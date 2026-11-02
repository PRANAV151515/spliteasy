package com.spliteasy.backend.group;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponse> create(@Valid @RequestBody GroupRequest request,
                                                Authentication authentication) {
        return ResponseEntity.ok(groupService.createGroup(request, authentication.getName()));
    }

    @GetMapping
    public List<GroupResponse> myGroups(Authentication authentication) {
        return groupService.getMyGroups(authentication.getName());
    }

    @GetMapping("/{id}")
    public GroupResponse getOne(@PathVariable Long id, Authentication authentication) {
        return groupService.getGroup(id, authentication.getName());
    }

    @PostMapping("/{id}/members")
    public GroupResponse addMember(@PathVariable Long id,
                                   @Valid @RequestBody AddMemberRequest request,
                                   Authentication authentication) {
        return groupService.addMember(id, request, authentication.getName());
    }
}

