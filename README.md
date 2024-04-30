# kyh_JPA
김영한의 JPA 로드맵 

### JPA 소개

- SQL 중점적인 개발의 문제점
    - Mapping 작업이 너무 오래걸린다.
    - 객체를 자바 컬렉션에 저장하듯이 저장할 수 없을까? → JPA
- JPA 소개
    - Java Persistence API
    - 자바 진영의 ORM 기술 표준
        
        *ORM? 객체와 관계형 DB와 매핑을 해주는 것
        
    - JPA는 애플리케이션과 JDBC 사이에서 동작
    - **패러다임 불일치 해결**
    - JPA가 만들어지기 전에 하이버네이트라는 오픈소스 ORM이 있었는데 이것을 본따 JPA를 만들었다.
- JPA를 사용하는 이유
    - SQL 중심적인 개발에서 객체 중심으로 개발
    - 생산성 : CRUD를 만들기 쉬움
    - 유지보수 : 필드 변경시 모든 SQL을 수정해야하는 문제를 해결
    - 패러다임 불일치 해결
        1. 상속
        저장 : 상속 관계로 있는 객체를 저장할 때 여러번 insert 코드를 작성하는 것이 아닌 .persist(object)의 하나의 코드로 알아서 insert 해줌
        조회 : 조인이 필요한 쿼리도 .find(object.class, id) 하나의 코드로 알아서 select 해줌
        2. 연관관계, 객체 그래프 탐색 : 연관 관계에 있는 테이블을 쉽게 저장하고 조회할 수 있음
        3. 신뢰할 수 있는 엔티티, 계층
        4. 동일한 트랜잭션에서 조회한 엔티티는 비교했을 때 같음을 보장함
            
            ```java
            Member member1 = jpa.find(Member.class, memberId);
            Member member2 = jpa.find(Member.class, memberId);
            member1 == member2; // true
            ```
            
    - 성능 최적화 기능
        1. 1차 캐시와 동일성 보장 : 같은 트랜잭션 안에서는 처음에는 sql을 날려서 반환하고 같은 엔티티를 조회했을 때 캐시된 엔티티를 들고옴(성능 향상)
        2. 트랜잭션을 지원하는 쓰기 지연
        INSERT : 트랜잭션을 커밋할 때까지 Insert Sql을 모았다가(bufferWirting) commit을 하는 순간 한번에 sql 실행
        3. 지연 로딩과 즉시 로딩 가능
        지연로딩: 객체가 실제 사용될 때 로딩
        즉시로딩: JOIN SQL로 한번에 연관된 객체까지 미리 조회
