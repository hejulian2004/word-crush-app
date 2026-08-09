package com.wordcrush.server.module.game.record.entity;

import com.wordcrush.server.common.persistence.BaseEntity;
import com.wordcrush.server.module.user.account.entity.UserAccount;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "game_records", uniqueConstraints = {
        @UniqueConstraint(name = "uk_game_records_record", columnNames = {"user_id", "game_type", "score", "played_at"})
})
public class GameRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(name = "game_type", nullable = false)
    private Integer gameType;

    @Column(nullable = false)
    private Integer score;

    @Column(name = "played_at", nullable = false)
    private LocalDateTime playedAt;

    @OrderBy("sortOrder ASC, id ASC")
    @OneToMany(mappedBy = "gameRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameRecordWord> learnedWords = new ArrayList<>();

    public void replaceLearnedWords(List<String> words) {
        learnedWords.clear();
        if (words == null) {
            return;
        }
        for (int i = 0; i < words.size(); i++) {
            GameRecordWord word = new GameRecordWord();
            word.setGameRecord(this);
            word.setSortOrder(i);
            word.setWordContent(words.get(i));
            learnedWords.add(word);
        }
    }
}
