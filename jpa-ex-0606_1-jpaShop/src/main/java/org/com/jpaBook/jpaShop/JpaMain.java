package org.com.jpaBook.jpaShop;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.com.jpaBook.jpaShop.domain.*;

public class JpaMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {

            Member member = new Member();
            member.setUsername("hello");
            member.setHomeAddress(new Address("city", "street", "100"));

            member.getFavoriteFoods().add("마라탕");
            member.getFavoriteFoods().add("탕수육");
            member.getFavoriteFoods().add("짬뽕");

            member.getAddressHistory().add(new Address("oldcity", "street", "100"));
            member.getAddressHistory().add(new Address("old2city", "street", "100"));
            member.getAddressHistory().add(new Address("old3city", "street", "100"));

            em.persist(member);

            em.flush();
            em.clear();

            Member findMember = em.find(Member.class, member.getId());

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
        } finally {
            em.close();

        }


        emf.close();
    }
}
