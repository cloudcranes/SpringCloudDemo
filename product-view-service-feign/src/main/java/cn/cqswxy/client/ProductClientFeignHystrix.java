package cn.cqswxy.client;

import cn.cqswxy.entity.Product;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * ProductClientFeignHystrix 实现了 ProductClientFeign 接口，并提供了 listProdcuts() 方法。
 * 这个方法就会固定返回包含一条信息的集合~
 */
@Component
public class ProductClientFeignHystrix implements ProductClientFeign {

    public List<Product> listProdcuts() {

        List<Product> result = new ArrayList<>();
        result.add(new Product(0, "产品数据微服务不可用", 0));
        return result;

    }

}
