package com.springboot.orderSystem.service;

import com.springboot.orderSystem.model.order;
import com.springboot.orderSystem.model.orderDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class orderService {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private List<order> orderList;

    public orderService() {
        orderList = new ArrayList<>();
        order order1 = new order("cust1", "Book", 350.000, 2);
        order order2 = new order("cust2", "Book", 30.12345, 1);
        orderList.addAll(Arrays.asList(order1, order2));
    }

    public List<order> getCart(String customerName, int page, int size) {
        String sql = "SELECT o.name AS order_name, o.type, o.price, o.qty, o.total " +
                "FROM dbo.customer c " +
                "INNER JOIN dbo.[order] o ON c.id = o.fk_customer_id " +
                "WHERE c.name = ? " +
                "ORDER BY o.id " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY;";
        int offset = page * size;
        return jdbcTemplate.query(sql,  new Object[]{customerName, offset, size}, (rs, rowNum) -> {
            order order = new order();
            order.setName(rs.getString("order_name"));
            order.setType(rs.getString("type"));
            order.setPrice(rs.getDouble("price"));
            order.setQty(rs.getInt("qty"));
            order.setTotal(rs.getDouble("total"));
            return order; //return list of order based on customerName
        });
    }

    public boolean addToCart(orderDTO order, String custName) {
        String sql = String.format("""
                        INSERT INTO dbo.[order] (fk_customer_id, name, type, price, qty, total, status)
                        VALUES (
                            (SELECT id FROM dbo.customer WHERE name = ?),
                            ?, ?, ?, ?, ?, ?
                        );""",
                custName,
                order.getName(),
                order.getType(),
                order.getPrice(),
                order.getQty(),
                order.getPrice() * order.getQty(),
                0
        );
        try {
            int rowsAffected = jdbcTemplate.update(sql);
            return rowsAffected > 0; // Return true if rowsAffected > 0
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean placeOrder(List<order> orders, String custName) {
        String sql = String.format("""
                        UPDATE dbo.[order]
                        SET status = 1
                        WHERE fk_customer_id = (SELECT id FROM dbo.customer WHERE name = ?)
                        AND name = ?;""");

        int maxRetries = 3;
        try {
            for (order ord : orders) {
                int retryAttempts = 0;
                boolean success = false;

                //retry 3 time just in case it fail to update.
                while (retryAttempts < maxRetries && !success) {
                    int rowsAffected = jdbcTemplate.update(sql,
                            custName,
                            ord.getName()       //order product's name
                    );
                    if (rowsAffected > 0) {
                        success = true; //Insert successful
                    } else {
                        retryAttempts++; //insert not successful, attempt +1
                    }
                }
                if (!success) {//still not update after attempted 3 retry.
                    return false;
                }
            }
            return true; // Successfully update all
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
