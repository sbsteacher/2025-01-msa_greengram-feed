package com.green.greengram.openfeign.user.model;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class UserGetItem {
    private String writerUid;
    private String writerNickName;
    private String writerPic;
}
