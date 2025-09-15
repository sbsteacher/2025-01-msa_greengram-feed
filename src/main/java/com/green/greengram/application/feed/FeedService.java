package com.green.greengram.application.feed;

import com.green.greengram.application.feed.model.FeedGetDto;
import com.green.greengram.application.feed.model.FeedGetRes;
import com.green.greengram.application.feed.model.FeedPostReq;
import com.green.greengram.application.feed.model.FeedPostRes;
import com.green.greengram.application.feedcomment.FeedCommentMapper;
import com.green.greengram.application.feedcomment.FeedCommentRepository;
import com.green.greengram.application.feedcomment.model.FeedCommentGetReq;
import com.green.greengram.application.feedcomment.model.FeedCommentGetRes;
import com.green.greengram.application.feedcomment.model.FeedCommentItem;

import com.green.greengram.configuration.model.ResultResponse;

import com.green.greengram.entity.UserId;
import com.green.greengram.configuration.constants.ConstComment;

import com.green.greengram.configuration.utils.MyFileManager;
import com.green.greengram.entity.Feed;

import com.green.greengram.openfeign.feedlike.FeedLikeClient;
import com.green.greengram.openfeign.feedlike.model.FeedLikeGetRes;
import com.green.greengram.openfeign.user.UserClient;
import com.green.greengram.openfeign.user.model.UserGetItem;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {
    private final FeedMapper feedMapper;
    private final FeedCommentMapper feedCommentMapper;
    //private final FeedLikeRepository feedLikeRepository;
    private final FeedRepository feedRepository;
    private final MyFileManager myFileManager;
    private final ConstComment constComment;
    private final FeedCommentRepository feedCommentRepository;
    private final UserClient userClient;
    private final FeedLikeClient feedLikeClient;

    @Transactional
    public FeedPostRes postFeed(long signedUserId, FeedPostReq req, List<MultipartFile> pics) {
        UserId writerUserId = new UserId(signedUserId);

        Feed feed = Feed.builder()
                        .writerUserId(writerUserId)
                        .location(req.getLocation())
                        .contents(req.getContents())
                        .build();

        feedRepository.save(feed); //feed객체는 영속성을 갖는다.

        List<String> fileNames = myFileManager.saveFeedPics(feed.getFeedId(), pics);

        feed.addFeedPics(fileNames);

        return new FeedPostRes(feed.getFeedId(), fileNames);
    }

    public List<FeedGetRes> getFeedList(FeedGetDto dto) {
        List<FeedGetRes> list = feedMapper.findAllLimitedTo(dto);
        if(list.size() == 0) {
            return list;
        }
        Set<Long> writerUserIdList = list.stream().map(item -> item.getWriterUserId()).collect(Collectors.toSet()); //각 피드당 writer_user_id값 가져오기

        List<Long> feedIdList = new ArrayList<>(list.size()); //feedId 수집용

        for(FeedGetRes feedGetRes : list) {
            feedIdList.add(feedGetRes.getFeedId()); //feedId수집

            feedGetRes.setPics(feedMapper.findAllPicByFeedId(feedGetRes.getFeedId()));

            //startIdx:0, size: 4
            FeedCommentGetReq req = new FeedCommentGetReq(feedGetRes.getFeedId(), constComment.startIndex, constComment.needForViewCount);
            List<FeedCommentItem> commentList = feedCommentMapper.findAllByFeedIdLimitedTo(req);
            boolean moreComment = commentList.size() > constComment.needForViewCount; //row수가 4였을 때만 true가 담기고, row수가 0~3인 경우는 false가 담긴다.
            FeedCommentGetRes feedCommentGetRes = new FeedCommentGetRes(moreComment, commentList);
            feedGetRes.setComments(feedCommentGetRes);
            if(moreComment) { //마지막 댓글 삭제
               commentList.remove(commentList.size() - 1); //마지막 아이템 삭제
            }

            //댓글 writer_user_id값 추가
            for(FeedCommentItem feedCommentItem : commentList) {
                writerUserIdList.add(feedCommentItem.getWriterUserId());
            }
        }

        //feed, comment 작성자 user_id값 수집 완료!!!

        //feed_id값 수집 완료!!

        ResultResponse<Map<Long, UserGetItem>> userRes = userClient.getUserList(writerUserIdList);
        log.info("userList: {}", userRes.getResult());
        Map<Long, UserGetItem> userMap = userRes.getResult();

        ResultResponse<Map<Long, FeedLikeGetRes>> feedLikeRes = feedLikeClient.getFeedLikeList(dto.getSignedUserId(), feedIdList);
        log.info("feedLikeList: {}", feedLikeRes.getResult());
        Map<Long, FeedLikeGetRes> feedLikeMap = feedLikeRes.getResult();

        for(FeedGetRes feedGetRes : list) {
            UserGetItem user = userMap.get(feedGetRes.getWriterUserId());
            feedGetRes.setWriterNickName(user.getWriterNickName());
            feedGetRes.setWriterUid(user.getWriterUid());
            feedGetRes.setWriterPic(user.getWriterPic());

            FeedLikeGetRes feedLike = feedLikeMap.get(feedGetRes.getFeedId());
            if(feedLike != null) {
                feedGetRes.setIsLike(feedLike.getIsLike());
                feedGetRes.setLikeCount(feedLike.getLikeCount());
            }

            for(FeedCommentItem feedCommentItem : feedGetRes.getComments().getCommentList()) {
                UserGetItem commentUser = userMap.get(feedCommentItem.getWriterUserId());
                feedCommentItem.setWriterNickName(commentUser.getWriterNickName());
                feedCommentItem.setWriterUid(commentUser.getWriterUid());
                feedCommentItem.setWriterPic(commentUser.getWriterPic());
            }
        }
        return list;
    }

    @Transactional
    public void deleteFeed(long signedUserId, long feedId) {
        Feed feed = feedRepository.findById(feedId)
                                  .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "feed_id가 존재하지 않습니다."));
        if(feed.getWriterUserId().getUserId() != signedUserId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "피드 삭제 권한이 없습니다.");
        }

        //해당 피드 좋아요 삭제
        //feedLikeRepository.deleteByIdFeedId(feedId);

        //해당 피드 댓글 삭제
        feedCommentRepository.deleteByFeedFeedId(feedId);

        //피드, 피드 사진 삭제
        feedRepository.delete(feed);

        //해당 피드 사진 폴더 삭제
        myFileManager.removeFeedDirectory(feedId);
    }

}
