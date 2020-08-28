package geektime.spring.springbucks.waiter.controller.request;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class NewOrderRequest {
    private String customer;
    private List<String> items;
}