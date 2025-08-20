package com.green.greengram.openfeign.user;

import com.green.greengram.configuration.FeignConfiguration;
import com.green.greengram.configuration.model.ResultResponse;
import com.green.greengram.openfeign.user.model.UserGetItem;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Set;

@FeignClient(name = "${constants.open-feign.user}", configuration = FeignConfiguration.class)
public interface UserClient {
    @GetMapping("/api/user")
    ResultResponse<Map<Long, UserGetItem>> getUserList(@RequestParam(name="writer_user_id") Set<Long> writerUserIdList);
}
