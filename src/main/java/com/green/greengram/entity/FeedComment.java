package com.green.greengram.entity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedComment extends UpdatedAt {
    @Id
    @Tsid
    private Long feedCommentId;

    @ManyToOne
    @JoinColumn(name="feed_id", nullable=false)
    private Feed feed;

    @Embedded
    private UserId writerUserId;

    @Column(nullable = false, length = 150)
    private String comment;
}
