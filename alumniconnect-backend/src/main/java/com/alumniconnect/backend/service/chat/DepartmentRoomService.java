package com.alumniconnect.backend.service.chat;

import com.alumniconnect.backend.entity.Conversation;
import com.alumniconnect.backend.entity.ConversationParticipant;
import com.alumniconnect.backend.entity.ConversationType;
import com.alumniconnect.backend.entity.Department;
import com.alumniconnect.backend.entity.Role;
import com.alumniconnect.backend.entity.User;
import com.alumniconnect.backend.repository.ConversationParticipantRepository;
import com.alumniconnect.backend.repository.ConversationRepository;
import com.alumniconnect.backend.repository.DepartmentRepository;
import com.alumniconnect.backend.repository.UserRepository;
import com.alumniconnect.backend.service.UserSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manages department group conversations (one per department, 23 total).
 * Admins are members of ALL department rooms.
 * Membership is self-healed lazily when a user loads their conversation list.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentRoomService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final UserSummaryService userSummaryService;

    /**
     * Return the department conversation for the given department, creating it if absent.
     * Caller: any role.
     */
    @Transactional
    public Conversation ensureRoom(Long departmentId) {
        return conversationRepository.findByDepartmentId(departmentId)
                .orElseGet(() -> {
                    Department dept = departmentRepository.getReferenceById(departmentId);
                    Conversation conv = new Conversation();
                    conv.setType(ConversationType.DEPARTMENT);
                    conv.setDepartment(dept);
                    return conversationRepository.save(conv);
                });
    }

    /**
     * Ensure all 23 department rooms exist. Called by DepartmentRoomInitializer at startup.
     */
    @Transactional
    public void ensureAll() {
        List<Department> departments = departmentRepository.findAll();
        for (Department dept : departments) {
            conversationRepository.findByDepartmentId(dept.getId())
                    .orElseGet(() -> {
                        Conversation conv = new Conversation();
                        conv.setType(ConversationType.DEPARTMENT);
                        conv.setDepartment(dept);
                        return conversationRepository.save(conv);
                    });
        }
        log.info("DepartmentRoomService: ensured {} department group rooms", departments.size());
    }

    /**
     * Add the user to their department room (or, if ADMIN, to ALL rooms) if not already a member.
     * Idempotent: safe to call repeatedly; only missing memberships are added.
     * Caller: any authenticated user.
     */
    @Transactional
    public void syncMembership(User user) {
        List<ConversationParticipant> existing = participantRepository.findAllForUser(user.getId());
        Set<Long> joinedConvIds = existing.stream()
                .map(p -> p.getConversation().getId())
                .collect(Collectors.toSet());

        if (user.getRole() == Role.ADMIN) {
            // Admins join all department rooms
            List<Conversation> allRooms = conversationRepository.findByType(ConversationType.DEPARTMENT);
            for (Conversation room : allRooms) {
                if (!joinedConvIds.contains(room.getId())) {
                    addParticipant(room, user);
                }
            }
        } else {
            // Students and alumni join their own department room
            Optional<Long> deptIdOpt = userSummaryService.departmentIdOf(user.getId());
            if (deptIdOpt.isEmpty()) return;

            Conversation room = ensureRoom(deptIdOpt.get());
            if (!joinedConvIds.contains(room.getId())) {
                addParticipant(room, user);
            }
        }
    }

    private void addParticipant(Conversation conv, User user) {
        ConversationParticipant p = new ConversationParticipant();
        p.setConversation(conv);
        p.setUser(user);
        participantRepository.save(p);
    }
}
