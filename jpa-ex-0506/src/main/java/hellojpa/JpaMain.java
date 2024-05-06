package hellojpa;

import jakarta.persistence.*;

import java.util.List;

public class JpaMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            // 저장코드
            Team t = new Team();
            t.setName("jedongTeam");
            em.persist(t);

            Member m = new Member();
            m.setUsername("jedong1");
            m.changeTeam(t);

            em.persist(m);

            em.flush();
            em.clear();

            Member findMember = em.find(Member.class, m.getId());

            // 단방향으로 team 찾기
//            Team findTeam = findMember.getTeam();
//            System.out.println("findTeam: " + findTeam.getId());
//            System.out.println("findTeam: " + findTeam.getName());

            // 양방향으로 members 찾기
            List<Member> members = findMember.getTeam().getMembers();

            for (Member member : members) {
                System.out.println("member: "+ member.getUsername());
            }


            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }
}
