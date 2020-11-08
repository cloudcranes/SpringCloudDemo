package cn.cqswxy.service;

import cn.cqswxy.client.ProductClientFeign;
import cn.cqswxy.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    ProductClientFeign productClientFeign;

    public List<Product> listProducts() {

        return productClientFeign.listProdcuts();

    }
}