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

### JPA 설정하기

- 프로젝트 환경
    - 자바17
    - 하이버네이트 6
    - h2 2.2.224
    - maven
- persistance.xml 설정하기
    - resource/META-INF/persistance.xml을 생성
    - 설정 중 원래 javax의 패키지 이름을 jakarta로 변경해야함
    - propertie설정 중 hibernate.dialect 설정*

        ```java
        <property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>
        ```

      데이터 베이스의 종류에 따라 특정 함수가 존재하는데 이런 차이에 따른 종속성을 없애기 위해 위 처럼 설정함
      *h2를 사용하면 H2Dialect, mysql을 사용하면 MySQLDialect


### JPA 사용

```java
EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
EntityManager em = emf.createEntityManager();
```

- EntityManagerFactory : 어플리케이션이 실행될 때 한번만 생성해서 계속 사용한다.
- EntityManager: 한 요청에 한번 사용하고 버린다.(쓰레드에서 공유하면 x)
- **JPA를 사용하면서 모든 데이터 변경은 트랜젝션 안에서 실행되어야 한다!!**
- JPQL

    ```java
    List<Member> list = em.createQuery("SELECT m FROM Member AS m" ,Member.class)
                    .getResultList();
    ```

    - 직접 쿼리를 작성해서 조회
    - SQL과 JPQL의 차이점: SQL은 db의 테이블을 대상으로 쿼리 / JPQL은 객체를 대상으로 쿼리

## 영속성 컨텍스트

- JPA를 통해 DB에 데이터를 넣기전에 엔티티의 상태를 관리하는 장소

### 영속성 컨텍스트의 생명주기

1. 비영속(new/transient) - 영속성 컨텍스트와 전혀 상관없는 새로운 상태
    
    ```java
    // 생성만 한 상태
    Member m = new Member();
    m.setId(1);
    m.setName("jedong");
    ```
    
2. 영속(managed) - 영속성 컨텍스트에서 관리되고 있는 상태 → 조회, 등록 시 영속상태가 됨
    
    ```java
    // 영속성 컨텍스트에 담김
    em.persist(m)
    ```
    
3. 준영속(detached) - 영속성 컨텍스트에 저장되었다가 분리된 상태
4. 삭제(remove) - 삭제된 상태

### 영속성 컨텍스트 존재의 이점

1. 1차 캐시
    - 엔티티를 key와 value 형태로 영속성 컨텍스트에 저장을 해놓고 DB에 조회할 때 캐시에 해당 데이터가 있으면 DB에 요청하지 않고 캐시 데이터를 사용한다. → DB 호출 횟수를 줄인다.
    - 하지만 하나의 Transaction에서 사용되는 1차 캐시이기 때문에 큰 이점은 없다.
2. 영속 엔티티의 동일성을 보장
    - 1차 캐시로 저장하기 때문에 여러번 같은 엔티티를 조회하면 동일성을 보장한다.
        
        ```java
        Member m1 = em.find(Member.class, 101);
        Member m2 = em.find(Member.class, 101);
        
        System.out.println(m1 == m2); // true
        ```
        
3. 트랜젝션을 지원하는 쓰기 지연
    - 엔티티를 저장했을 때 바로 insert하지 않고 영속성 컨텍스트에 ‘1차 캐시’안에 넣고 ‘**쓰기SQL 저장소**’에  담아놓았다가 commit이 실행될 때 한번에 insert쿼리를 처리한다.
4. 엔티티 변경 감지(dirty checking)
    - 엔티티를 영속성 컨텍스트에 넣은 후 값을 변경하기만 하면 자바의 컬렉션처럼 그 값이 변경됨
    - 처음 엔티티가 영속성 컨텍스트에 담겼을 때 스냅샷을 찍어놓고 트랜젝션이 commit되는 시점에 변경을 감지해서 update문을 실행한다.

### Flush

- 영속성 컨텍스트의 저장된 데이터를 DB에 반영하는 것
- 영송성 컨텍스트를 flush하는 방법
    1. 직접 flush하기 ex.flush()
    2. 트랜젝션 커밋(자동 반영)
    3. JPQL 쿼리 수행
- flush가 실행될 때 영속성 컨텍스트가 비워지는 것이 아닌 해당 쿼리만 즉시 실행하는 것

## 엔티티 맵핑

### @Entity

- 이 어노테이션이 붙어야만 JPA에서 관리한다.
- **기본 생성자 필수**(protect, public으로 만들어야함)

### 기타 정보

- @Column 어노테이션의 속성은 ddl생성시에만 이용하고 런타임 중에는 영향을 주지 않는다.(속성으로 입력한 코드들은 ddl 생성할 때만 영향이 있다!)