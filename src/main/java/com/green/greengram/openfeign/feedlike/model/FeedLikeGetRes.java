package com.green.greengram.openfeign.feedlike.model;

import lombok.Getter;

@Getter
public class FeedLikeGetRes {
    private Long feedId;
    private int likeCount;
    private int isLike;
}
