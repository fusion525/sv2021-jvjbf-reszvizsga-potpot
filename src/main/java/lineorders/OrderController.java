package lineorders;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderDTO> listOrders() {
        return orderService.listOrders();
    }

    @GetMapping("/{month}")
    public OrderDTO getOrdersByMonth(@PathVariable("month") int month) {
        return orderService.getOrdersByMonth(month);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDTO createOrder(@RequestBody CreateOrderCommand command){
        return orderService.createOrder(command);
    }

    @PutMapping("/{id}/shipping")
    public OrderDTO readyForShipping(@PathVariable("id") long id, @RequestBody ShippingCommand command) {
        return orderService.readyForShipping(id, command);
    }

    @GetMapping("/shippingIncome")
    public int shippingIncome() {
        return orderService.shippingIncome();
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrders(){
        orderService.deleteOrders();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Problem> handleNotFound(IllegalArgumentException iae) {
        Problem problem =
                Problem.builder()
                        .withType(URI.create("orders/not-found")).withTitle("Not found").withStatus(Status.NOT_FOUND)
                        .withDetail(iae.getMessage()).build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Problem> invalidParameters(IllegalStateException ise) {
        Problem problem =
                Problem.builder()
                        .withType(URI.create("orders")).withTitle("Invalid Parameter").withStatus(Status.BAD_REQUEST)
                        .withDetail(ise.getMessage()).build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

}
