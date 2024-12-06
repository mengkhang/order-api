package com.springboot.orderSystem.controller;
import com.springboot.orderSystem.model.order;
import com.springboot.orderSystem.model.orderDTO;
import com.springboot.orderSystem.service.orderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/order")
public class orderController {

    private orderService order_Service;

    @Autowired
    public orderController (orderService order_Service){
        this.order_Service = order_Service;
    }

    // Add to cart
    @PostMapping("/addToCart/{customerName}")
    public boolean addToCart(@RequestBody orderDTO order, @PathVariable String customerName) {
        return order_Service.addToCart(order, customerName);
    }

    // get Cart
    @GetMapping("/getCart/{customerName}")
    public List<order> getCart(@PathVariable String customerName, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return order_Service.getCart(customerName, page, size);
    }

    //place order
    @PutMapping("/placeOrder/{customerName}")
    public boolean placeOrder(@RequestBody List<order> order, @PathVariable String customerName) {
        return order_Service.placeOrder(order, customerName);
    }
}
