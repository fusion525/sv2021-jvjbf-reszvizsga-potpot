package lineorders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderControllerRestIT {

    @Autowired
    TestRestTemplate template;

    @BeforeEach
    void init() {
        template.delete("/api/orders");
    }


    @Test
    void testCreateOrder(){
        OrderDTO result =
                template.postForObject("/api/orders",new CreateOrderCommand("DK976ZH"),OrderDTO.class);

        assertEquals("DK976ZH",result.getProductNumber());
        assertEquals(0,result.getShippingPrice());
        assertEquals(LocalDate.now(),result.getOrderDate());
        assertEquals(null, result.getShippingDate());
    }

    @Test
    void testGetOrders(){
        template.postForObject("/api/orders",new CreateOrderCommand("DK776ZH"),OrderDTO.class);
        template.postForObject("/api/orders",new CreateOrderCommand("DK976ZH"),OrderDTO.class);


        List<OrderDTO> result = template.exchange(
                "/api/orders",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<OrderDTO>>() {
                }).getBody();


        assertThat(result).extracting(OrderDTO::getProductNumber).containsExactly("DK776ZH","DK976ZH");
    }


    @Test
    void testGetOrdersByMonth(){
        template.postForObject("/api/orders",new CreateOrderCommand("DK776ZH"),OrderDTO.class);
        template.postForObject("/api/orders",new CreateOrderCommand("DK976ZH"),OrderDTO.class);

        String url = "/api/orders?month="+LocalDate.now().getMonthValue();
        List<OrderDTO> result = template.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<OrderDTO>>() {
                }).getBody();

        assertThat(result).extracting(OrderDTO::getProductNumber).containsExactly("DK776ZH","DK976ZH");
    }

    @Test
    void testReadyForShipping(){
        template.postForObject("/api/orders",new CreateOrderCommand("DK776ZH"),OrderDTO.class);

        template.put("/api/orders/1/shipping",new ShippingCommand(2000));

        OrderDTO result = template.getForObject("/api/orders/1",OrderDTO.class);

        assertEquals(LocalDate.now(),result.getShippingDate());
        assertEquals(2000,result.getShippingPrice());

    }

    @Test
    void testShippingIncome(){
        template.postForObject("/api/orders",new CreateOrderCommand("DK776ZH"),OrderDTO.class);
        template.postForObject("/api/orders",new CreateOrderCommand("DK976ZH"),OrderDTO.class);

        template.put("/api/orders/1/shipping",new ShippingCommand(2000));
        template.put("/api/orders/2/shipping",new ShippingCommand(3000));

       int result = template.getForObject("/api/orders/shippingIncome",Integer.class);

       assertEquals(5000,result);
    }

    @Test
    void createOrderWithInvalidProductNumber(){
       Problem result = template.postForObject("/api/orders",new CreateOrderCommand(""),Problem.class);

       assertEquals(Status.BAD_REQUEST,result.getStatus());
    }

    @Test
    void orderNotFoundTest(){
        Problem result = template.getForObject("/api/orders/1", Problem.class);

        assertEquals(URI.create("order/not-found"),result.getType());
        assertEquals(Status.NOT_FOUND, result.getStatus());
    }


}
