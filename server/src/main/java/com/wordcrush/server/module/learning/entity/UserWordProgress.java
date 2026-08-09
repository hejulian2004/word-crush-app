package com.wordcrush.server.module.learning.entity;

import com.wordcrush.server.common.persistence.BaseEntity;
import com.wordcrush.server.module.user.account.entity.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "user_word_progress", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_word_progress_user_word", columnNames = {"user_id", "word_id"})
})
public class UserWordProgress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "word_id", nullable = false)
    private LearningWord word;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(name = "master_count", nullable = false)
    private Integer masterCount = 0;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(name = "is_mastered", nullable = false)
    private Boolean mastered = false;

    @Column(nullable = false)
    private Long version = 0L;
}
