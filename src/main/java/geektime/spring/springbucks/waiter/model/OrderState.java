package geektime.spring.springbucks.waiter.model;

import com.google.common.collect.Maps;
import lombok.Getter;

import java.util.Map;
import java.util.Objects;

@Getter
public enum OrderState {
    INIT(0),
    PAID(1),
    BREWING(2),
    BREWED(4),
    TAKEN(4),
    CANCELLED(5);

    private final int value;

    private static final Map<Integer, OrderState> STATE_MAP = Maps.newHashMap();

    static {
        for (OrderState state : OrderState.values()) {
            STATE_MAP.put(state.getValue(), state);
        }
    }

    OrderState(int value) {
        this.value = value;
    }

    public static OrderState findByCode(Integer code) {
        if(Objects.isNull(code)) {
            return null;
        }
        return STATE_MAP.get(code);
    }
}
