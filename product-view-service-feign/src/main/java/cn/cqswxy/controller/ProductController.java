package cn.cqswxy.controller;

import cn.cqswxy.entity.Product;
import cn.cqswxy.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class ProductController {

    @Autowired
    ProductService productService;

    @Value("${version}")
    String version;

    @RequestMapping("/products")
    public Object products(Model model) {
        List<Product> listProducts = productService.listProducts();
        model.addAttribute("version", version);
        model.addAttribute("listProducts", listProducts);
        return "products";
    }
}
