package com.clickstechnology.Brillo.Mall.domain.conversation;

import com.clickstechnology.Brillo.Mall.application.enums.ConversationMode;
import com.clickstechnology.Brillo.Mall.application.enums.ConversationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByReference(String reference);
    Optional<Conversation> findByWhatsappConversationIdAndChannelKeyAndConversationMode(
            String whatsappConversationId,
            String channelKey,
            ConversationMode conversationMode);
    Optional<Conversation> findTopByWhatsappConversationIdAndChannelKeyAndConversationModeInOrderByLastInteractionAtDesc(
            String whatsappConversationId,
            String channelKey,
            List<ConversationMode> conversationModes);
    List<Conversation> findAllByWhatsappConversationId(String whatsappConversationId);
    @Query("""
            select count(distinct c.whatsappConversationId)
            from Conversation c
            where c.entryBusinessId = :businessId
              and c.conversationMode in :conversationModes
            """)
    long countDistinctWhatsappConversationIdByEntryBusinessIdAndConversationModeIn(
            @Param("businessId") String businessId,
            @Param("conversationModes") List<ConversationMode> conversationModes);
    long countByActiveBusinessIdAndConversationModeInAndStatusIn(
            String activeBusinessId,
            List<ConversationMode> conversationModes,
            List<ConversationStatus> statuses);

    @Query("""
            select c
            from Conversation c
            where c.businessId = :businessId
               or c.entryBusinessId = :businessId
               or c.activeBusinessId = :businessId
            order by c.lastInteractionAt desc, c.updatedAt desc
            """)
    Page<Conversation> findVisibleByBusinessId(
            @Param("businessId") String businessId,
            Pageable pageable);

    @Query("""
            select c
            from Conversation c
            where c.businessId = :businessId
               or c.entryBusinessId = :businessId
               or c.activeBusinessId = :businessId
            order by c.lastInteractionAt desc, c.updatedAt desc
            """)
    List<Conversation> findAllVisibleByBusinessId(
            @Param("businessId") String businessId);

    @Query("""
            select c
            from Conversation c
            where c.reference = :reference
              and (c.businessId = :businessId
                   or c.entryBusinessId = :businessId
                   or c.activeBusinessId = :businessId)
            """)
    Optional<Conversation> findVisibleByBusinessIdAndReference(
            @Param("businessId") String businessId,
            @Param("reference") String reference);
}
