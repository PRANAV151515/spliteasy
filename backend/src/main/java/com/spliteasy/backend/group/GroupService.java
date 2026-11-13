package com.spliteasy.backend.group;

import com.spliteasy.backend.user.User;
import com.spliteasy.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Transactional
    public GroupResponse createGroup(GroupRequest request, String currentEmail) {
        User creator = findUser(currentEmail);

        Group group = new Group();
        group.setName(request.name());
        group.setDescription(request.description());
        group.setCreatedBy(creator);
        group.getMembers().add(creator);

        return GroupResponse.from(groupRepository.save(group));
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> getMyGroups(String currentEmail) {
        return groupRepository.findByMembers_Email(currentEmail)
                .stream()
                .map(GroupResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupResponse getGroup(Long groupId, String currentEmail) {
        Group group = findGroup(groupId);
        checkMember(group, currentEmail);
        return GroupResponse.from(group);
    }

    @Transactional
    public GroupResponse addMember(Long groupId, AddMemberRequest request, String currentEmail) {
        Group group = findGroup(groupId);
        checkMember(group, currentEmail);

        User newMember = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("No registered user found with this email"));

        if (group.getMembers().contains(newMember)) {
            throw new IllegalArgumentException("This user is already in the group");
        }

        group.getMembers().add(newMember);
        return GroupResponse.from(groupRepository.save(group));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private Group findGroup(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
    }

    private void checkMember(Group group, String email) {
        boolean isMember = group.getMembers().stream()
                .anyMatch(m -> m.getEmail().equals(email));
        if (!isMember) {
            throw new IllegalArgumentException("You are not a member of this group");
        }
    }
}

