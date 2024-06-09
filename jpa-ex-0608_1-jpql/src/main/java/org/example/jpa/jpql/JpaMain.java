package org.example.jpa.jpql;

import jakarta.persistence.*;

import java.util.List;

public class JpaMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Team team = new Team();
            team.setName("team1");

            em.persist(team);

            Member member = new Member();

            member.setUsername("hi");
            member.setAge(10);
            member.setTeam(team);

            Member member2 = new Member();
            member2.setUsername("hi2");
            member2.setTeam(team);

            em.persist(member);
            em.persist(member2);

            // TypeQuery
//            TypedQuery<Member> query = em.createQuery("SELECT m FROM Member as m", Member.class);
//            for (Member member1 : query.getResultList()) {
//                System.out.println("member1 :" + member1);
//            }

            // 파라미터
//            TypedQuery<Member> query = em.createQuery("SELECT m FROM Member as m WHERE username = :username", Member.class);
//            query.setParameter("username", "hi");
//            Member findMember = query.getSingleResult();
//            System.out.println("member   :  " + findMember);

            // new 명령어로 스칼라 타입 조회
//            List<MemberDTO> result = em.createQuery("SELECT new org.example.jpa.jpql.MemberDTO(m.username, m.age) FROM Member m")
//                    .getResultList();
//            MemberDTO m = result.get(0);
//            System.out.println("resutl : " + m.getUsername());

            // 페이징
//            for (int i = 0; i < 100; i++) {
//                Member member2 = new Member();
//
//                member2.setUsername("hi" + i);
//                member2.setAge(10 + i);
//
//                em.persist(member2);
//            }
//
            em.flush();
            em.clear();
//
//            List<Member> result = em.createQuery("SELECT m FROM Member m order by m.age desc", Member.class)
//                    .setFirstResult(1)
//                    .setMaxResults(10)
//                    .getResultList();
//
//            for (Member member1 : result) {
//                System.out.println(member1);
//            }
//            System.out.println();
//            System.out.println();

            List<Member> result = em.createQuery("SELECT m FROM Member m join m.team")
                    .getResultList();

            for (Member member1 : result) {
                System.out.println("member : " + member1.getUsername() + ", " + member1.getTeam().getName());
            }




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
