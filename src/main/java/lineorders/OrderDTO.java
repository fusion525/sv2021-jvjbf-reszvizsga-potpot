package lineorders;

import lombok.Data;

import java.time.LocalDate;

@Data
public class OrderDTO {

    public long id;
    public String productNumber;
    public int shippingPrice;
    public LocalDate orderDate;
    public LocalDate shippingDate;

}
