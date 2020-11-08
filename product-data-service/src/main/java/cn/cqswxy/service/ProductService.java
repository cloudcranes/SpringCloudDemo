package cn.cqswxy.service;


import cn.cqswxy.pojo.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {
    @Value("${server.port}")
    String port;

    public List<Product> listProducts() {
        List<Product> ps = new ArrayList<>();
        ps.add(new Product(1, "product a from port:" + port, 50));
        ps.add(new Product(2, "product b from port:" + port, 100));
        ps.add(new Product(3, "product c from port:" + port, 150));
        ps.add(new Product(4, "product c from port:" + port, 200));
        ps.add(new Product(5, "product c from port:" + port, 250));
        ps.add(new Product(6, "product c from port:" + port, 300));
        return ps;
    }
}
