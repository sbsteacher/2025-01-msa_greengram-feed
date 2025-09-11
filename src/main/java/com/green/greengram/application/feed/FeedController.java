package com.green.greengram.application.feed;

import com.green.greengram.application.feed.model.*;
import com.green.greengram.configuration.constants.ConstFile;
import com.green.greengram.configuration.model.ResultResponse;
import com.green.greengram.configuration.model.SignedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FeedController {
    private final FeedService feedService;
    private final ConstFile constFile;

    @PostMapping
    public ResultResponse<?> postFeed(@AuthenticationPrincipal SignedUser userPrincipal
                                    , @Valid @RequestPart FeedPostReq req
                                    , @RequestPart(name = "pic") List<MultipartFile> pics) {

        if(pics.size() > constFile.maxPicCount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST
                    , String.format("사진은 %d장까지 선택 가능합니다.", constFile.maxPicCount));
        }
        log.info("signedUserId: {}", userPrincipal.signedUserId);
        log.info("req: {}", req);
        log.info("pics.size(): {}", pics.size());
        FeedPostRes result = feedService.postFeed(userPrincipal.signedUserId, req, pics);
        return new ResultResponse<>("피드 등록 완료", result);
    }

    //페이징, 피드(사진, 댓글(3개만))
    //현재는 피드+사진만 (N+1로 처리)
    @GetMapping
    public ResultResponse<?> getFeedList(@AuthenticationPrincipal SignedUser signedUser
                                       , @Valid @ModelAttribute FeedGetReq req) {
        log.info("signedUserId: {}", signedUser.signedUserId);
        log.info("req: {}", req);
        FeedGetDto feedGetDto = FeedGetDto.builder()
                                          .signedUserId(signedUser.signedUserId)
                                          .startIdx((req.getPage() - 1) * req.getRowPerPage())
                                          .size(req.getRowPerPage())
                                          .profileUserId(req.getProfileUserId())
                                          .build();
        List<FeedGetRes> result = feedService.getFeedList(feedGetDto);
        return new ResultResponse<>(String.format("rows: %d", result.size()), result);
    }

    @DeleteMapping
    public ResultResponse<?> deleteFeed(@AuthenticationPrincipal SignedUser signedUser
                                      , @RequestParam("feed_id") @Valid @Positive Long feedId) {
        log.info("signedUserId: {}", signedUser.signedUserId);
        log.info("feedId: {}", feedId);
        feedService.deleteFeed(signedUser.signedUserId, feedId);
        return new ResultResponse<>("피드가 삭제되었습니다.", null);
    }


    //Spring Security를 사용하지 않았다면 SCG에서 전달해주는 signedUserId값은 아래처럼 받아서 사용해야 한다.
    @DeleteMapping("/delete")
    public ResultResponse<?> deleteFeed2(HttpServletRequest request
            , @RequestParam("feed_id") @Valid @Positive Long feedId) {

        String signedUserId = request.getHeader("signedUser");
        Long singUserId = Long.parseLong(signedUserId);


        log.info("signedUserId: {}", singUserId);
        log.info("feedId: {}", feedId);
        feedService.deleteFeed(singUserId, feedId);
        return new ResultResponse<>("피드가 삭제되었습니다.", null);
    }
}
