package com.wordcrush.server.module.learning.entity;

import com.wordcrush.server.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "learning_words")
public class LearningWord extends BaseEntity {

    @Id
    private Integer id;

    @Column(nullable = false, length = 128)
    private String english;

    @Column(nullable = false, length = 255)
    private String pronunciation;

    @Column(nullable = false, length = 1024)
    private String chinese;

    @Column(name = "content_version", nullable = false)
    private Long contentVersion = 1L;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false)
    private Integer status = 1;
}
