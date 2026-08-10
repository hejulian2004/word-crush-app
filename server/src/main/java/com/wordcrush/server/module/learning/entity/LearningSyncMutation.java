package com.wordcrush.server.module.learning.entity;

import com.wordcrush.server.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "learning_sync_mutations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_learning_sync_mutations_user_mutation", columnNames = {"user_id", "mutation_id"})
})
public class LearningSyncMutation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "mutation_id", nullable = false, length = 96)
    private String mutationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id")
    private LearningWord word;

    @Column(nullable = false, length = 32)
    private String operation;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(name = "master_count")
    private Integer masterCount;

    @Column(name = "daily_target")
    private Integer dailyTarget;

    @Column(name = "client_at", length = 64)
    private String clientAt;
}
