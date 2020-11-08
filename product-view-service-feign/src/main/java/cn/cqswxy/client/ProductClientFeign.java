package cn.cqswxy.client;

import cn.cqswxy.entity.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 注解由原来的
 *
 * @FeignClient(value = "PRODUCT-DATA-SERVICE")
 * 修改为
 * @FeignClient(value = "PRODUCT-DATA-SERVICE",fallback = ProductClientFeignHystrix.class)
 * <p>
 * 这就表示，如果访问的 PRODUCT-DATA-SERVICE 不可用的话，就调用 ProductClientFeignHystrix 来进行反馈信息。
 */

//@FeignClient(value = "PRODUCT-DATA-SERVICE")
@FeignClient(value = "PRODUCT-DATA-SERVICE", fallback = ProductClientFeignHystrix.class)
public interface ProductClientFeign {

    @GetMapping("/products")
    public List<Product> listProdcuts();
}