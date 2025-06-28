package ru.practicum.stats.analyzer.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "user_actions")
@IdClass(UserActionId.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class UserAction {
    @Id
    @Column(name = "user_id")
    Long userId;
    @Id
    @Column(name = "event_id")
    Long eventId;
    @Column(name = "user_score")
    Double score;
    @Column(name = "timestamp_action")
    Instant timestamp;
}
