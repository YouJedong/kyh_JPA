package hellojpa;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;

public class JpaMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            //JPQL
//            List<Member> result = em.createQuery("select m from Member m where m.username like '%kim%'",
//                    Member.class
//            ).getResultList();
//
//            for (Member member: result) {
//                System.out.println(member);
//            }

            // Criteria
//            CriteriaBuilder cb = em.getCriteriaBuilder();
//            CriteriaQuery<Member> query = cb.createQuery(Member.class);
//
//            Root<Member> m = query.from(Member.class);
//
//            CriteriaQuery<Member> cq = query.select(m);
//
//            // 동적쿼리
//            String username = "dposakd";
//            if (username != null) {
//                cq = cq.where(cb.equal(m.get("username"), "kim"));
//            }
//
//            List<Member> resultList = em.createQuery(cq)
//                            .getResultList();




            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }
}
