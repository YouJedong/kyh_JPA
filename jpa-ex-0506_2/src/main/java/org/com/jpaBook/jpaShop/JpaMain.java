package org.com.jpaBook.jpaShop;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.com.jpaBook.jpaShop.domain.Member;
import org.com.jpaBook.jpaShop.domain.Order;
import org.com.jpaBook.jpaShop.domain.OrderStatus;

public class JpaMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Member member = new Member();
            member.setName("jedong");
            em.persist(member);

            Order order = new Order();
            order.addMember(member);
            order.setStatus(OrderStatus.ORDER);
            em.persist(order);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();

        }


        emf.close();
    }
}
