package com.green.greengram.openfeign.feedlike;

import com.green.greengram.configuration.FeignConfiguration;
import com.green.greengram.configuration.model.ResultResponse;
import com.green.greengram.openfeign.feedlike.model.FeedLikeGetRes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "${constants.open-feign.feed-like}", configuration = FeignConfiguration.class)
public interface FeedLikeClient {
    @GetMapping("/api/feed/like")
    ResultResponse<Map<Long, FeedLikeGetRes>> getFeedLikeList(@RequestParam("signed_user_id") Long signedUserId
                                                            , @RequestParam("feed_id") List<Long> feedIdList);
}
