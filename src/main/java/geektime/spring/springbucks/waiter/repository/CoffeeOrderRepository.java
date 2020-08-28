package geektime.spring.springbucks.waiter.repository;

import com.google.common.collect.Lists;
import geektime.spring.springbucks.waiter.model.Coffee;
import geektime.spring.springbucks.waiter.model.CoffeeOrder;
import geektime.spring.springbucks.waiter.model.OrderState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

@Repository
public class CoffeeOrderRepository {
    @Autowired
    private DatabaseClient databaseClient;

    public Mono<CoffeeOrder> get(Long id) {
        return databaseClient.execute(String.format("select * from t_order where id = %d", id))
                .map((r, rm) -> CoffeeOrder.builder().id(id).customer(r.get("customer", String.class))
                        .state(OrderState.findByCode(r.get("state", Integer.class)))
                        .createTime(r.get("create_time", Date.class))
                        .updateTime(r.get("update_time", Date.class))
                        .items(Lists.newArrayList())
                        .build())
                .first()
                .flatMap(o -> databaseClient.execute(String.format("select c.* from t_coffee c, t_order_coffee oc " +
                        "where c.id = oc.items_id and oc.coffee_order_id = %d ", id))
                        .as(Coffee.class)
                        .fetch()
                        .all()
                        .collectList()
                        .flatMap(l -> {
                            o.getItems().addAll(l);
                            return Mono.just(o);
                        })
                );
    }

    public Mono<Long> save(CoffeeOrder order) {
        return databaseClient.insert().into("t_order")
                .value("customer", order.getCustomer())
                .value("state", order.getState().ordinal())
                .value("create_time", new Timestamp(order.getCreateTime().getTime()))
                .value("update_time", new Timestamp(order.getUpdateTime().getTime()))
                .fetch()
                .first()
                .flatMap(m -> Mono.just((Long) m.get("ID")))
                .flatMap(id -> Flux.fromIterable(order.getItems())
                        .flatMap(c -> databaseClient.insert().into("t_order_coffee")
                                .value("coffee_order_id", id)
                                .value("items_id", c.getId())
                                .then()).then(Mono.just(id)));

    }
}
