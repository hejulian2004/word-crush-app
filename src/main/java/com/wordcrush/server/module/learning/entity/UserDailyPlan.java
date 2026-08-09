package com.wordcrush.server.module.learning.entity;

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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_daily_plans", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_daily_plans_user_date", columnNames = {"user_id", "plan_date"})
})
public class UserDailyPlan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(name = "plan_date", nullable = false)
    private LocalDate planDate;

    @Column(name = "daily_target", nullable = false)
    private Integer dailyTarget;

    @OrderBy("sortOrder ASC, id ASC")
    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserDailyPlanItem> items = new ArrayList<>();

    public void replaceItems(List<LearningWord> words) {
        items.clear();
        for (int i = 0; i < words.size(); i++) {
            UserDailyPlanItem item = new UserDailyPlanItem();
            item.setPlan(this);
            item.setWord(words.get(i));
            item.setSortOrder(i);
            items.add(item);
        }
    }
}
