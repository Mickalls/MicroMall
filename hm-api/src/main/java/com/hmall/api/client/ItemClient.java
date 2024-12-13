package com.hmall.api.client;

import com.hmall.api.dto.ItemDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

@FeignClient(value = "item-service") // 远程调用的服务名称
public interface ItemClient {

    /**
     * Get http://item-service/items?ids=args1,args2,...argsn
     * @param ids
     * @return
     */
    @GetMapping("/items") // 远程调用的请求方式和路径
    List<ItemDTO> queryItemByIds(@RequestParam("ids") Collection<Long> ids); // 远程调用的请求参数和返回值类型
}
