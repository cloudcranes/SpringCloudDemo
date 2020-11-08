package cn.cqswxy.controller;

import cn.cqswxy.entity.Product;
import cn.cqswxy.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class ProductController {

    @Autowired
    ProductService productService;

    @RequestMapping("/products")
    public Object products(Model model) {
        List<Product> productList = productService.listProducts();
        model.addAttribute("productList", productList);
        return model;
    }
}
