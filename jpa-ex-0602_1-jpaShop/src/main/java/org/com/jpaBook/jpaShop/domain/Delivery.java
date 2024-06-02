package org.com.jpaBook.jpaShop.domain;

import jakarta.persistence.*;

import static jakarta.persistence.FetchType.*;

@Entity
public class Delivery extends BaseEntity {

    @Id @GeneratedValue
    private Long id;

    @OneToOne(mappedBy = "delivery", fetch = LAZY)
    private Order order;

    private String city;
    private String street;
    private String zipcode;
    private DeliveryStatus status;
}
