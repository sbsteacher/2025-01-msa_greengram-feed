package com.green.greengram.configuration.utils;

import com.green.greengram.configuration.constants.ConstFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MyFileManager {
    private final ConstFile constFile;
    private final MyFileUtils myFileUtils;

    public List<String> saveFeedPics(long feedId, List<MultipartFile> pics) {
        //폴더 생성
        String directory = String.format("%s/%s/%d", constFile.uploadDirectory, constFile.feedPic, feedId);
        myFileUtils.makeFolders(directory);

        List<String> randomFileNames = new ArrayList<>(pics.size());
        for(MultipartFile pic : pics) {
            String randomFileName = myFileUtils.makeRandomFileName(pic); //랜덤파일 이름 생성
            randomFileNames.add(randomFileName); //리턴할 randomFileNames에 이름 추가

            String savePath = directory + "/" + randomFileName;
            try {
                myFileUtils.transferTo(pic, savePath);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "피드 이미지 저장에 실패하였습니다.");
            }
        }

        return randomFileNames;
    }

    private String makeFeedDirectoryPath(long feedId) {
        return String.format("%s/%s/%d",  constFile.uploadDirectory, constFile.feedPic, feedId);
    }

    //피드 폴더 삭제
    public void removeFeedDirectory(long feedId) {
        String directory = makeFeedDirectoryPath(feedId);
        myFileUtils.deleteFolder(directory, true);
    }
}
