package lineorders;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private ModelMapper modelMapper;

    private AtomicLong idGenerator = new AtomicLong();

    private List<Order> orders = new ArrayList<>();

    public OrderService(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public List<OrderDTO> listOrders() {
        Type targetListType = new TypeToken<List<OrderDTO>>(){}.getType();

        List<Order> filteredOrders = orders.stream().filter(o -> o.getOrderDate().getMonthValue() == LocalDate.now().getMonthValue())
                .collect(Collectors.toList());

        return modelMapper.map(filteredOrders, targetListType);
    }

    public OrderDTO getOrderById(Long id) {
        return modelMapper.map(orders.stream().filter(o -> o.getId() == id).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Order not found by id: " + id))
                , OrderDTO.class);
    }

    public OrderDTO createOrder(CreateOrderCommand command) {

        if (command.getProductNumber().isEmpty()) {
            throw new IllegalStateException();
        }

        Order order = new Order(idGenerator.incrementAndGet(),command.getProductNumber(),0,LocalDate.now(),null);

        orders.add(order);

        return modelMapper.map(order, OrderDTO.class);

    }


    public OrderDTO readyForShipping(long id, ShippingCommand command) {

        Order order = orders.stream().filter(o-> o.getId() == id).findFirst().orElseThrow(() -> new IllegalArgumentException("Order not Found: " + id));

                if (command.getPrice() < 0) {
                    throw new IllegalStateException();
                }

                if (order.getShippingDate() != null) {
                    order.setShippingDate(LocalDate.now());
                    order.setShippingPrice(command.getPrice());
                }

                return modelMapper.map(order, OrderDTO.class);
    }

    public int shippingIncome() {
        int sum = orders.stream().map(o -> o.getShippingPrice()).mapToInt(Integer::intValue).sum();

        return sum;
    }

    public void deleteOrders() {
        orders.clear();
    }
}
