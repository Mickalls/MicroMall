package com.hmall.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;

@FeignClient(value = "cart-service")
public interface CartClient {

    /**
     * 批量删除购物车中的商品条目(每一个条目对应一个具体商品,有num字段标识购物车中对于这个商品的存储数量(
     * @param ids 传入的
     */
    @DeleteMapping("/carts")
    void removeByItemIds(@RequestParam("ids") Collection<Long> ids);
}
