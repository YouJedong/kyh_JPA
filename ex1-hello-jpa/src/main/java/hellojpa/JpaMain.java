package hellojpa;

import jakarta.persistence.*;

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
//        List<MemberBefore0505> list = em.createQuery("SELECT m FROM Member AS m" , MemberBefore0505.class)
//                .setFirstResult(5) // 시작 row설정
//                .setMaxResults(10) // 가져올 row 갯수 설정
//                .getResultList();
//
//        for (MemberBefore0505 member : list) {
//            System.out.println("member.getName : " + member.getName());
//        }
        Member0505 m = new Member0505();
        m.setId(1L);
        m.setName("jedong0505");
        m.setRoleType(RoleType.USER);
        em.persist(m);

        tx.commit();

        em.close();
        emf.close();
    }
}
