package com.hmall.api.client;

import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@FeignClient(value = "item-service") // 远程调用的服务名称
public interface ItemClient {

    /**
     * 根据若干id查询对应商品
     * Get http://item-service/items?ids=args1,args2,...argsn
     * @param ids 商品ip集合
     * @return
     */
    @GetMapping("/items") // 远程调用的请求方式和路径
    List<ItemDTO> queryItemByIds(@RequestParam("ids") Collection<Long> ids); // 远程调用的请求参数和返回值类型

    /**
     * 批量扣减库存
     * Put http://item-service/items/stock/deduct?items=?,?...?
     * @param items 订单细节列表
     */
    @PutMapping("/items/stock/deduct")
    void deductStock(@RequestBody List<OrderDetailDTO> items);
}
