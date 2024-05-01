package hellojpa;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

public class JpaMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // 저장
//        Member m = new Member();
//        m.setId(2L);
//        m.setName("jedong2");
//        em.persist(m);

        // JPQL 사용
        List<Member> list = em.createQuery("SELECT m FROM Member AS m" ,Member.class)
                .setFirstResult(5) // 시작 row설정
                .setMaxResults(10) // 가져올 row 갯수 설정
                .getResultList();

        for (Member member : list) {
            System.out.println("member.getName : " + member.getName());
        }

        tx.commit();

        em.close();
        emf.close();
    }
}
